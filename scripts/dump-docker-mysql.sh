#!/bin/bash
#
# Script to dump all data from Docker MySQL
# This includes table structure and all data
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
BACKUP_FILE="${BACKUP_DIR}/mysql_dump_${TIMESTAMP}.sql"
LOG_FILE="${BACKUP_DIR}/dump_${TIMESTAMP}.log"

# Docker MySQL configuration (via SSH)
DOCKER_SSH_HOST="${DOCKER_SSH_HOST}"
DOCKER_SSH_USER="${DOCKER_SSH_USER:-$(whoami)}"
DOCKER_SSH_PORT="${DOCKER_SSH_PORT:-22}"
DOCKER_CONTAINER_NAME="${DOCKER_MYSQL_CONTAINER:-esl-mysql}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD}"
MYSQL_DATABASE="${MYSQL_DATABASE:-esl}"

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

# Create backup directory if it doesn't exist
mkdir -p "${BACKUP_DIR}"

log "Starting MySQL dump from Docker container..."
log "Docker host: ${DOCKER_SSH_USER}@${DOCKER_SSH_HOST}:${DOCKER_SSH_PORT} (via SSH)"
log "Container: ${DOCKER_CONTAINER_NAME}"
log "Database: ${MYSQL_DATABASE}"
log "Backup file: ${BACKUP_FILE}"

# Check if SSH host is configured
if [ -z "${DOCKER_SSH_HOST}" ]; then
    error "DOCKER_SSH_HOST environment variable is required"
    exit 1
fi

DOCKER_CMD="ssh -p ${DOCKER_SSH_PORT} ${DOCKER_SSH_USER}@${DOCKER_SSH_HOST} docker"

# Find the actual Docker container name (handles Swarm service names with hashes)
log "Finding Docker container with name pattern: ${DOCKER_CONTAINER_NAME}"
ACTUAL_CONTAINER_NAME=$(${DOCKER_CMD} ps --filter "name=${DOCKER_CONTAINER_NAME}" --format '{{.Names}}')

if [ -z "${ACTUAL_CONTAINER_NAME}" ]; then
    error "Docker container matching '${DOCKER_CONTAINER_NAME}' is not running!"
    error "Available containers:"
    ${DOCKER_CMD} ps --format '{{.Names}}'
    exit 1
fi

log "Found Docker container: ${ACTUAL_CONTAINER_NAME}"
log "Docker container is running. Proceeding with dump..."

# Perform the dump
# Key insight: --skip-lock-tables causes mysqldump to try FLUSH TABLES which requires RELOAD privilege
# Solution: Remove --skip-lock-tables and let mysqldump use default table locking (works without special privileges)
# 
# Flags used:
# --no-tablespaces: skip TABLESPACE info (requires PROCESS privilege)
# --set-gtid-purged=OFF: don't add GTID info (requires SUPER/REPLICATION_CLIENT privilege)
# --routines: dump stored procedures and functions
# --triggers: dump triggers
# --events: dump events
# --hex-blob: dump binary columns using hexadecimal notation
# --complete-insert: use complete INSERT statements with column names
# --add-drop-database: add DROP DATABASE statements
# --databases: specify database(s) to dump (includes CREATE DATABASE statements)
log "Dumping database structure and data..."

if [ -n "${MYSQL_PASSWORD}" ]; then
    ssh -p "${DOCKER_SSH_PORT}" "${DOCKER_SSH_USER}@${DOCKER_SSH_HOST}" \
        "docker exec ${ACTUAL_CONTAINER_NAME} mysqldump \
        -u${MYSQL_USER} \
        -p${MYSQL_PASSWORD} \
        --no-tablespaces \
        --set-gtid-purged=OFF \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        --complete-insert \
        --add-drop-database \
        --databases ${MYSQL_DATABASE}" \
        > "${BACKUP_FILE}" 2>> "${LOG_FILE}"
else
    ssh -p "${DOCKER_SSH_PORT}" "${DOCKER_SSH_USER}@${DOCKER_SSH_HOST}" \
        "docker exec ${ACTUAL_CONTAINER_NAME} mysqldump \
        -u${MYSQL_USER} \
        --no-tablespaces \
        --set-gtid-purged=OFF \
        --routines \
        --triggers \
        --events \
        --hex-blob \
        --complete-insert \
        --add-drop-database \
        --databases ${MYSQL_DATABASE}" \
        > "${BACKUP_FILE}" 2>> "${LOG_FILE}"
fi

DUMP_STATUS=$?

if [ ${DUMP_STATUS} -eq 0 ]; then
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
    log "Dump completed successfully!"
    log "Backup size: ${BACKUP_SIZE}"
    log "Backup location: ${BACKUP_FILE}"
    
    # Create a symlink to the latest backup
    ln -sf "$(basename ${BACKUP_FILE})" "${BACKUP_DIR}/latest.sql"
    log "Created symlink: ${BACKUP_DIR}/latest.sql"
    
    # Verify the dump file
    if [ -s "${BACKUP_FILE}" ]; then
        log "Backup file is valid (non-empty)"
    else
        error "Backup file is empty!"
        exit 1
    fi
else
    error "Dump failed with status code: ${DUMP_STATUS}"
    error "Check the log file for details: ${LOG_FILE}"
    exit 1
fi

log "Dump operation completed successfully!"
echo ""
echo "Next step: Run import-to-k8s-mysql.sh to restore to K8s MySQL"
