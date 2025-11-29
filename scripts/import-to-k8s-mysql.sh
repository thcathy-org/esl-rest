#!/bin/bash
#
# Script to import MySQL dump into K8s MySQL
# This will restore the complete database including structure and data
#

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load environment variables from .env file if present
if [ -f "${SCRIPT_DIR}/../.env" ]; then
    source "${SCRIPT_DIR}/../.env"
fi

BACKUP_DIR="${SCRIPT_DIR}/backup"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
LOG_FILE="${BACKUP_DIR}/import_${TIMESTAMP}.log"

# K8s MySQL configuration
K8S_CONTEXT="${K8S_CONTEXT}"
K8S_NAMESPACE="${K8S_NAMESPACE:-default}"
K8S_POD_PREFIX="${K8S_POD_PREFIX:-esl-rest-mysql}"
K8S_MYSQL_USER="${K8S_MYSQL_USER:-root}"
K8S_MYSQL_PASSWORD="${K8S_MYSQL_PASSWORD}"
K8S_MYSQL_DATABASE="${K8S_MYSQL_DATABASE:-esl}"

# Backup file to import (defaults to latest)
BACKUP_FILE="${BACKUP_FILE:-${BACKUP_DIR}/latest.sql}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1" | tee -a "${LOG_FILE}"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "${LOG_FILE}"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1" | tee -a "${LOG_FILE}"
}

# SSH Configuration for Jump Host (if K8s API is not directly accessible)
K8S_SSH_HOST="${K8S_SSH_HOST}"
K8S_SSH_USER="${K8S_SSH_USER}"
K8S_SSH_PORT="${K8S_SSH_PORT:-22}"

if [ -n "${K8S_SSH_HOST}" ]; then
    # Resolve SSH destination
    SSH_DEST="${K8S_SSH_HOST}"
    if [ -n "${K8S_SSH_USER}" ]; then
        SSH_DEST="${K8S_SSH_USER}@${K8S_SSH_HOST}"
    fi
    
    log "K8S_SSH_HOST specified: ${K8S_SSH_HOST}"
    log "Running in proxy mode - transferring files to jump host..."
    
    # Check backup file existence locally before transferring
    if [ ! -f "${BACKUP_FILE}" ]; then
        error "Backup file not found: ${BACKUP_FILE}"
        exit 1
    fi

    # Create unique remote directory
    REMOTE_ROOT="/tmp/k8s_import_$(date +%s)"
    log "Creating remote directory: ${REMOTE_ROOT}"
    ssh -p "${K8S_SSH_PORT}" "${SSH_DEST}" "mkdir -p ${REMOTE_ROOT}"
    
    if [ $? -ne 0 ]; then
        error "Failed to create remote directory on ${K8S_SSH_HOST}"
        exit 1
    fi

    # Check for kubeconfig on remote host first
    log "Checking for kubeconfig on remote host..."
    REMOTE_KUBECONFIG=$(ssh -p "${K8S_SSH_PORT}" "${SSH_DEST}" "if [ -f \$HOME/.kube/config ]; then echo \$HOME/.kube/config; elif [ -f /etc/rancher/k3s/k3s.yaml ] && [ -r /etc/rancher/k3s/k3s.yaml ]; then echo /etc/rancher/k3s/k3s.yaml; else echo ''; fi")
    
    if [ -z "${REMOTE_KUBECONFIG}" ]; then
        error "No accessible kubeconfig found on remote host"
        exit 1
    fi
    
    log "Found kubeconfig at: ${REMOTE_KUBECONFIG}"
    
    # Create environment file locally
    TEMP_ENV=$(mktemp)
    cat <<EOF > "${TEMP_ENV}"
export K8S_CONTEXT='${K8S_CONTEXT}'
export K8S_NAMESPACE='${K8S_NAMESPACE}'
export K8S_POD_PREFIX='${K8S_POD_PREFIX}'
export K8S_MYSQL_USER='${K8S_MYSQL_USER}'
export K8S_MYSQL_PASSWORD='${K8S_MYSQL_PASSWORD//\'/\'\\\'\'}'
export K8S_MYSQL_DATABASE='${K8S_MYSQL_DATABASE}'
export BACKUP_FILE='${REMOTE_ROOT}/$(basename "${BACKUP_FILE}")'
export KUBECONFIG='${REMOTE_KUBECONFIG}'
EOF

    # Transfer files
    log "Transferring backup file: $(basename "${BACKUP_FILE}")..."
    scp -P "${K8S_SSH_PORT}" "${BACKUP_FILE}" "${SSH_DEST}:${REMOTE_ROOT}/"
    
    log "Transferring script and environment..."
    scp -P "${K8S_SSH_PORT}" "${TEMP_ENV}" "${SSH_DEST}:${REMOTE_ROOT}/.env"
    scp -P "${K8S_SSH_PORT}" "${BASH_SOURCE[0]}" "${SSH_DEST}:${REMOTE_ROOT}/import.sh"
    
    rm -f "${TEMP_ENV}"
    
    # Execute remotely
    log "Executing import script on remote host..."
    log "---------------------------------------------------"
    # Use bash login shell to ensure environment (including KUBECONFIG) is loaded
    ssh -p "${K8S_SSH_PORT}" -t "${SSH_DEST}" "bash -l -c 'cd ${REMOTE_ROOT} && source .env && chmod +x import.sh && ./import.sh'"
    REMOTE_EXIT_CODE=$?
    log "---------------------------------------------------"
    
    # Cleanup
    log "Cleaning up remote files..."
    ssh -p "${K8S_SSH_PORT}" "${SSH_DEST}" "rm -rf ${REMOTE_ROOT}"
    
    if [ ${REMOTE_EXIT_CODE} -eq 0 ]; then
        log "Remote execution completed successfully."
        exit 0
    else
        error "Remote execution failed with exit code ${REMOTE_EXIT_CODE}."
        exit ${REMOTE_EXIT_CODE}
    fi
fi

# Create backup directory if it doesn't exist
mkdir -p "${BACKUP_DIR}"

# Set kubectl command with context if specified
if [ -n "${K8S_CONTEXT}" ]; then
    KUBECTL="kubectl --context=${K8S_CONTEXT}"
    log "Using K8s context: ${K8S_CONTEXT}"
else
    KUBECTL="kubectl"
    CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
    log "Using current K8s context: ${CURRENT_CONTEXT}"
fi

log "Starting MySQL import to K8s..."
log "Namespace: ${K8S_NAMESPACE}"
log "Pod prefix: ${K8S_POD_PREFIX}"
log "Database: ${K8S_MYSQL_DATABASE}"
log "Backup file: ${BACKUP_FILE}"

# Check if backup file exists
if [ ! -f "${BACKUP_FILE}" ]; then
    error "Backup file not found: ${BACKUP_FILE}"
    error "Please run dump-docker-mysql.sh first or specify BACKUP_FILE environment variable"
    exit 1
fi

# Get the K8s pod name
log "Finding K8s MySQL pod..."
ERR_FILE=$(mktemp)
if ! K8S_POD=$(${KUBECTL} get pods -n "${K8S_NAMESPACE}" -l "app=${K8S_POD_PREFIX}" -o jsonpath='{.items[0].metadata.name}' 2> "${ERR_FILE}"); then
    echo -e "${RED}[ERROR]${NC} kubectl command failed:" | tee -a "${LOG_FILE}"
    cat "${ERR_FILE}" | tee -a "${LOG_FILE}"
    rm -f "${ERR_FILE}"
    exit 1
fi
rm -f "${ERR_FILE}"

if [ -z "${K8S_POD}" ]; then
    error "Could not find K8s MySQL pod with prefix '${K8S_POD_PREFIX}' in namespace '${K8S_NAMESPACE}'"
    error "Available pods:"
    ${KUBECTL} get pods -n "${K8S_NAMESPACE}" | tee -a "${LOG_FILE}"
    exit 1
fi

log "Found K8s pod: ${K8S_POD}"

# Check if pod is running
POD_STATUS=$(${KUBECTL} get pod "${K8S_POD}" -n "${K8S_NAMESPACE}" -o jsonpath='{.status.phase}')
if [ "${POD_STATUS}" != "Running" ]; then
    error "K8s pod is not running. Current status: ${POD_STATUS}"
    exit 1
fi

log "K8s pod is running. Proceeding with import..."

# Import the dump file
log "Importing database from: ${BACKUP_FILE}"
BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
log "Import file size: ${BACKUP_SIZE}"

# Copy the dump file to the pod
log "Copying dump file to K8s pod..."
${KUBECTL} cp "${BACKUP_FILE}" "${K8S_NAMESPACE}/${K8S_POD}:/tmp/mysql_import.sql"

if [ $? -ne 0 ]; then
    error "Failed to copy dump file to K8s pod"
    exit 1
fi

log "Dump file copied successfully"

# Verify remote file size
REMOTE_SIZE=$(${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- sh -c 'ls -lh /tmp/mysql_import.sql' | awk '{print $5}')
log "Remote file size: ${REMOTE_SIZE}"

# Recreate database to ensure clean import
log "Recreating database '${K8S_MYSQL_DATABASE}'..."

# We connect without specifying a database first to drop/create
RECREATE_CMD="DROP DATABASE IF EXISTS \`${K8S_MYSQL_DATABASE}\`; CREATE DATABASE \`${K8S_MYSQL_DATABASE}\`;"

if [ -n "${K8S_MYSQL_PASSWORD}" ]; then
    ${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- mysql \
        -u"${K8S_MYSQL_USER}" \
        -p"${K8S_MYSQL_PASSWORD}" \
        -e "${RECREATE_CMD}"
else
    ${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- sh -c \
        "mysql -u${K8S_MYSQL_USER} -e \"${RECREATE_CMD}\""
fi

if [ $? -ne 0 ]; then
    error "Failed to recreate database"
    exit 1
fi

log "Database recreated successfully"

# Import the database
log "Importing database (this may take several minutes)..."

# Use the file inside the pod for import
if [ -n "${K8S_MYSQL_PASSWORD}" ]; then
    ${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- sh -c \
        "mysql -u'${K8S_MYSQL_USER}' -p'${K8S_MYSQL_PASSWORD}' '${K8S_MYSQL_DATABASE}' < /tmp/mysql_import.sql"
else
    ${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- sh -c \
        "mysql -u'${K8S_MYSQL_USER}' '${K8S_MYSQL_DATABASE}' < /tmp/mysql_import.sql"
fi

IMPORT_STATUS=$?

# Clean up the temporary file
${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- rm -f /tmp/mysql_import.sql || true

if [ ${IMPORT_STATUS} -eq 0 ]; then
    log "Import completed successfully!"
    
    # Verify the import
    log "Verifying import..."
    TABLE_COUNT=$(${KUBECTL} exec -n "${K8S_NAMESPACE}" "${K8S_POD}" -- mysql \
        -u"${K8S_MYSQL_USER}" \
        $([ -n "${K8S_MYSQL_PASSWORD}" ] && echo "-p${K8S_MYSQL_PASSWORD}") \
        -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${K8S_MYSQL_DATABASE}';" -sN)
    
    log "Number of tables in database: ${TABLE_COUNT}"
    
    if [ "${TABLE_COUNT}" -gt 0 ]; then
        log "Database verification successful!"
    else
        warning "Database appears to be empty. Please verify manually."
    fi
else
    error "Import failed with status code: ${IMPORT_STATUS}"
    error "Check the log file for details: ${LOG_FILE}"
    exit 1
fi

log "Import operation completed successfully!"
echo ""
echo "Database has been successfully copied from Docker MySQL to K8s MySQL"
