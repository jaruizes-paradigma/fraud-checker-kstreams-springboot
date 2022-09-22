#!/bin/bash

PLATFORM=$(pwd)/../platform

# remove infrastructure
cd "$PLATFORM"
docker-compose down

# remove <none> images
docker image prune