#!/bin/bash
#
# Main script to migrate MySQL from Docker to K8s
# This orchestrates the dump and import process
#
# Usage:
#   ./migrate-mysql-docker-to-k8s.sh [OPTIONS]
#
# Options:
#   --dry-run          Check prerequisites and show config without executing
#   --dump-only        Only run the dump step
#   --import-only      Only run the import step (requires existing dump)
#   --help             Show this help message
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Load environment variables from .env file if present
if [ -f "${SCRIPT_DIR}/../.env" ]; then
    source "${SCRIPT_DIR}/../.env"
fi

BACKUP_DIR="${SCRIPT_DIR}/backup"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Parse command line arguments
DRY_RUN=false
DUMP_ONLY=false
IMPORT_ONLY=false

for arg in "$@"; do
    case $arg in
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        --dump-only)
            DUMP_ONLY=true
            shift
            ;;
        --import-only)
            IMPORT_ONLY=true
            shift
            ;;
        --help)
            head -n 15 "$0" | tail -n 12
            exit 0
            ;;
        *)
            echo "Unknown option: $arg"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo ""
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}  MySQL Migration: Docker → K8s${NC}"
    echo -e "${BLUE}============================================${NC}"
    echo ""
}

print_section() {
    echo ""
    echo -e "${BLUE}--------------------------------------------${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}--------------------------------------------${NC}"
    echo ""
}

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

check_prerequisites() {
    print_section "Checking Prerequisites"
    
    local missing_deps=()
    
    # Check for kubectl
    if ! command -v kubectl &> /dev/null; then
        missing_deps+=("kubectl")
    else
        log "✓ kubectl is installed"
    fi
    
    # Check for SSH
    if ! command -v ssh &> /dev/null; then
        missing_deps+=("ssh")
    else
        log "✓ SSH is installed"
    fi
    
    # Check if DOCKER_SSH_HOST is set
    if [ -z "${DOCKER_SSH_HOST}" ]; then
        missing_deps+=("DOCKER_SSH_HOST environment variable not set")
    else
        # Test SSH connection to Docker host
        log "Testing SSH connection to Docker host..."
        if ssh -p "${DOCKER_SSH_PORT:-22}" "${DOCKER_SSH_USER:-$(whoami)}@${DOCKER_SSH_HOST}" "docker --version" &> /dev/null; then
            log "✓ SSH connection to Docker host successful"
        else
            missing_deps+=("Cannot connect to Docker host via SSH: ${DOCKER_SSH_USER:-$(whoami)}@${DOCKER_SSH_HOST}")
        fi
    fi
    
    # Check K8s context
    if [ -n "${K8S_CONTEXT}" ]; then
        log "Checking K8s context: ${K8S_CONTEXT}"
        if kubectl config get-contexts "${K8S_CONTEXT}" &> /dev/null; then
            log "✓ K8s context '${K8S_CONTEXT}' is valid"
        else
            missing_deps+=("K8s context '${K8S_CONTEXT}' not found")
        fi
    else
        CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "")
        if [ -z "${CURRENT_CONTEXT}" ]; then
            missing_deps+=("No K8s context set. Set K8S_CONTEXT or configure kubectl context")
        else
            log "✓ Using current K8s context: ${CURRENT_CONTEXT}"
        fi
    fi
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        error "Missing required dependencies:"
        for dep in "${missing_deps[@]}"; do
            echo "  - ${dep}"
        done
        exit 1
    fi
    
    log "All prerequisites met!"
}

show_configuration() {
    print_section "Configuration"
    
    echo "Source (Docker via SSH):"
    echo "  Host:      ${DOCKER_SSH_USER:-$(whoami)}@${DOCKER_SSH_HOST}:${DOCKER_SSH_PORT:-22}"
    echo "  Container: ${DOCKER_MYSQL_CONTAINER:-esl-mysql (default)}"
    echo "  Database:  ${MYSQL_DATABASE:-esl (default)}"
    echo "  User:      ${MYSQL_USER:-root (default)}"
    echo ""
    echo "Target (K8s):"
    if [ -n "${K8S_CONTEXT}" ]; then
        echo "  Context:   ${K8S_CONTEXT}"
    else
        CURRENT_CONTEXT=$(kubectl config current-context 2>/dev/null || echo "none")
        echo "  Context:   ${CURRENT_CONTEXT} (current)"
    fi
    echo "  Namespace: ${K8S_NAMESPACE:-default (default)}"
    echo "  Pod:       ${K8S_POD_PREFIX:-esl-rest-mysql (default)}"
    echo "  Database:  ${MYSQL_DATABASE:-esl (default)}"
    echo "  User:      ${MYSQL_USER:-root (default)}"
    echo ""
    echo "Backup Directory: ${BACKUP_DIR}"
    echo ""
}

confirm_migration() {
    if [ "${AUTO_CONFIRM}" = "true" ]; then
        return 0
    fi
    
    warning "This will REPLACE all data in the K8s MySQL database!"
    echo -n "Are you sure you want to continue? (yes/no): "
    read -r response
    
    if [ "${response}" != "yes" ]; then
        echo "Migration cancelled."
        exit 0
    fi
}

run_dump() {
    print_section "Step 1: Dumping from Docker MySQL"
    
    if [ ! -f "${SCRIPT_DIR}/dump-docker-mysql.sh" ]; then
        error "dump-docker-mysql.sh not found!"
        exit 1
    fi
    
    log "Starting dump process..."
    bash "${SCRIPT_DIR}/dump-docker-mysql.sh"
    
    if [ $? -ne 0 ]; then
        error "Dump failed!"
        exit 1
    fi
    
    log "Dump completed successfully!"
}

run_import() {
    print_section "Step 2: Importing to K8s MySQL"
    
    if [ ! -f "${SCRIPT_DIR}/import-to-k8s-mysql.sh" ]; then
        error "import-to-k8s-mysql.sh not found!"
        exit 1
    fi
    
    log "Starting import process..."
    bash "${SCRIPT_DIR}/import-to-k8s-mysql.sh"
    
    if [ $? -ne 0 ]; then
        error "Import failed!"
        exit 1
    fi
    
    log "Import completed successfully!"
}

print_summary() {
    print_section "Migration Summary"
    
    log "✓ Database dumped from Docker MySQL"
    log "✓ Database imported to K8s MySQL"
    log "✓ Migration completed successfully!"
    
    echo ""
    echo "Backup files location: ${BACKUP_DIR}"
    echo "Latest dump: ${BACKUP_DIR}/latest.sql"
    echo ""
    
    if [ -f "${BACKUP_DIR}/k8s_backup_before_import_${TIMESTAMP}.sql" ]; then
        echo "Original K8s database backup: ${BACKUP_DIR}/k8s_backup_before_import_*.sql"
        echo "(You can use this to rollback if needed)"
        echo ""
    fi
}

# Main execution
main() {
    print_header
    
    # Check prerequisites
    check_prerequisites
    
    # Show configuration
    show_configuration
    
    # Exit if dry-run
    if [ "$DRY_RUN" = true ]; then
        echo ""
        log "Dry-run mode - no changes will be made"
        echo ""
        exit 0
    fi
    
    # Confirm migration
    confirm_migration
    
    # Create backup directory
    mkdir -p "${BACKUP_DIR}"
    
    # Run dump (unless import-only)
    if [ "$IMPORT_ONLY" = false ]; then
        run_dump
    fi
    
    # Exit if dump-only
    if [ "$DUMP_ONLY" = true ]; then
        echo ""
        log "Dump-only mode - stopping before import"
        echo ""
        exit 0
    fi
    
    # Run import (unless dump-only)
    if [ "$DUMP_ONLY" = false ]; then
        run_import
    fi
    
    # Print summary
    print_summary
    
    echo -e "${GREEN}Migration completed successfully!${NC}"
    echo ""
}

# Run main function
main
