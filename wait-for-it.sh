#!/bin/bash

# wait-for-it.sh

# Exit if no arguments are given
if [ $# -lt 1 ]; then
  echo "Usage: wait-for-it.sh host:port [-t timeout] -- command"
  exit 1
fi

# Set up parameters
hostport=$1
timeout=30
shift

# Parse options
while getopts "t:" opt; do
  case $opt in
    t)
      timeout=$OPTARG
      ;;
    *)
      echo "Usage: wait-for-it.sh host:port [-t timeout] -- command"
      exit 1
      ;;
  esac
done

# Wait for the service to be available
echo "Waiting for $hostport to be available..."
start_ts=$(date +%s)
while ! nc -z ${hostport%:*} ${hostport#*:}; do
  sleep 1
  # Timeout logic
  end_ts=$(date +%s)
  elapsed=$(( end_ts - start_ts ))
  if [ "$elapsed" -gt "$timeout" ]; then
    echo "Timeout reached. Could not connect to $hostport"
    exit 1
  fi
done

echo "$hostport is available."

# Execute the remaining command
exec "$@"