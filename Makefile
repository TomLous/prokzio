#########################################
# Environment variables that need to be set in the Github Secrets settings

# CODECOV_TOKEN => https://app.codecov.io/gh/TomLous/prokzio

#########################################

# Note:
# - Many make targets are dependant on sbt config & actions
# - Defining values to be deferred (if not needed or sbt not present they should not be expanded).
#   However when they are expanded they should be only expanded once (lazy evaluation)
#   The way to do this is: OUTPUT = $(eval OUTPUT := $$(shell some-comand))$(OUTPUT) due to the ltr expansion and recursive nature
#   How http://make.mad-scientist.net/deferred-simple-variable-expansion/

# Hardcoded values

# The remote registry for Docker containers
CONTAINER_REGISTRY :=  ghcr.io

# The path where artifacts are created
OUTPUT_PATH := ./output

# Use Bash instead of sh
SHELL := /bin/bash

SBT_COMMAND := sbt --error 'set showSuccess := false' -Dsbt.log.noformat=true

# The version of the current release
VERSION = $(eval VERSION := $$(shell $(SBT_COMMAND) showVersion))$(VERSION)

# Allow to pass the module name as command line arg
MODULE = $(shell arg="$(filter-out $@,$(MAKECMDGOALS))" && echo $${arg:-${1}})


# Get the image namespace/repo
IMAGE_NAME = $(eval IMAGE_NAME := $$(shell $(SBT_COMMAND) $(MODULE)/showImageName))$(IMAGE_NAME)

define check_module
	@$(if $(MODULE), $(info Using module: $(MODULE)), $(error Module is not set in command (make [action] [module]).))
endef


.DEFAULT_GOAL := version

.PHONY: version
version:
	@echo $(VERSION)


# Build Commands
.PHONY: graal-build-local
graal-build-local:
	@$(call check_module)
	$(SBT_COMMAND) $(MODULE)/nativeImage

.PHONY: graal-build-docker-local
graal-build-docker-local:
	@$(call check_module)
	$(SBT_COMMAND) $(MODULE)/graalvm-native-image:packageBin
	$(SBT_COMMAND) $(MODULE)/docker:publishLocal


.PHONY: docker-image-clean
docker-image-clean:
	@$(call check_module)
	$(info Deleting these images:)
	@docker images  -f "reference=$(IMAGE_NAME):*" --format "{{.Repository}}:{{.Tag}}"
	@docker rmi $$(docker images -f "reference=$(IMAGE_NAME):*" --format "{{.Repository}}:{{.Tag}}")

.PHONY: docker-images-clean
docker-images-clean:
	$(info Deleting these images:)
	@docker images  -f "reference=$(IMAGE_NAMESPACE)/*:*" --format "{{.Repository}}:{{.Tag}}"
	@docker rmi $$(docker images -f "reference=$(IMAGE_NAMESPACE)/*:*" --format "{{.Repository}}:{{.Tag}}")

.PHONY: docker-images-purge
docker-images-purge:
	@-docker rmi $$(docker images -f "dangling=true" -q)
	@-docker system prune



# Guard to check ENV vars
guard-%:
	@ if [ -z '${${*}}' ]; then echo 'Environment variable $* not set.' && exit 1; fi

# Catch all for module name arguments
%:
	@:
