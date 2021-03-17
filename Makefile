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

# Feature name
FEATURE = $(shell echo $(MODULE) | sed -e 's/[^a-zA-Z0-9]/-/g' | tr '[:upper:]' '[:lower:]' )

# Available modules to build
MODULES = $(eval MODULES :=  $$(shell $(SBT_COMMAND) listModules))$(MODULES) #Weird bug adding some escape char to all output

# Get the image namespace/repo
IMAGE_NAME = $(eval IMAGE_NAME := $$(shell $(SBT_COMMAND) $(MODULE)/showImageName))$(IMAGE_NAME)


####

define check_module
	@$(if $(MODULE), $(info Using module: $(MODULE)), $(error Module is not set in command (make [action] [module]). Use one of the following modules as argument: $(MODULES)))
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

.PHONY: graal-build-docker
graal-build-docker:
	@$(call check_module)
	$(SBT_COMMAND) $(MODULE)/graalvm-native-image:packageBin
	$(SBT_COMMAND) $(MODULE)/service/docker:publishLocal #TODO Maybe setup remote publishing in sbt?

.PHONY: docker-push-registry
docker-push-registry: guard-REGISTRY_OWNER
	@$(call check_module)
	@docker tag $(IMAGE_NAME):$(VERSION) $(CONTAINER_REGISTRY)/$(REGISTRY_OWNER)/$(IMAGE_NAME):$(VERSION)
	@docker tag $(IMAGE_NAME):$(VERSION) $(CONTAINER_REGISTRY)/$(REGISTRY_OWNER)/$(IMAGE_NAME):latest
	@docker push $(CONTAINER_REGISTRY)/$(REGISTRY_OWNER)/$(IMAGE_NAME):$(VERSION)
	@docker push $(CONTAINER_REGISTRY)/$(REGISTRY_OWNER)/$(IMAGE_NAME):latest

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


# Github Container Registry Commands
.PHONY: registry-docker-push-login
registry-docker-push-login: guard-REGISTRY_PASSWORD guard-CONTAINER_REGISTRY guard-REGISTRY_USERNAME
	@echo $(REGISTRY_PASSWORD) | docker login $(CONTAINER_REGISTRY) --username $(REGISTRY_USERNAME) --password-stdin

.PHONY: registry-list-images
registry-list-images: guard-REGISTRY_PASSWORD guard-CHART_REGISTRY guard-CHART_USERNAME guard-REPO_NAME
	@curl -s -u $(CHART_USERNAME):$(CHART_PASSWORD) -X GET https://$(CHART_REGISTRY)/v2/_catalog?n=2000 | jq '.[] | .[] | select( startswith ("$(REPO_NAME)/")  and (contains("/charts/") | not))'

.PHONY: registry-repository-tags
registry-repository-tags: guard-REGISTRY_PASSWORD guard-CHART_REGISTRY guard-CHART_USERNAME guard-ENV_REPOSITORY
	curl -s -u $(CHART_USERNAME):$(CHART_PASSWORD) -X GET https://$(CHART_REGISTRY)/v2/$(ENV_REPOSITORY)/tags/list | jq '.[]'



# Guard to check ENV vars
guard-%:
	@ if [ -z '${${*}}' ]; then echo 'Environment variable $* not set.' && exit 1; fi

# Catch all for module name arguments
%:
	@:
