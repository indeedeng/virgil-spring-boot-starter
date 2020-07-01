#!/bin/bash

#####
# tagForRelease.sh
#
# Our publish server cannot handle running the npm/frontend steps required to build the artifacts, so this script
# will
# 1) build the frontend (buildFrontEndProd)
# 2) Increment the version in gradle (to whatever was passed in)
# 3) Commit to master
# 4) Create tag using version
#####

#Assign flags to variables
while getopts v: flag
do
    case "${flag}" in
        v) NEW_VERSION=${OPTARG};;
    esac
done

#Verify given version is valid
if [[ "$NEW_VERSION" == "" ]]; then
  echo "Version (-v) flag must have a value in format #.#.#"
  exit 1;
fi

#Verify new version is #.#.# format
if [[ $(echo "$NEW_VERSION" | grep -oE "[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+") == "" ]]; then
  echo "Version (-v) flag is not a valid format.  Expected format #.#.#. [Received: ${NEW_VERSION}]"
  exit 1;
fi


# Verify that branch is set to master
#CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
#
## fail if not set
#if [[ "$CURRENT_BRANCH" != "master" ]]; then
#  echo "Change to master branch before running this script. [Current Branch: $CURRENT_BRANCH]"
#  exit 1
#fi

## Debug logs
#echo "$CURRENT_BRANCH"



# build npm resources
cd .. # Change working directory to parent
./gradlew buildFrontEndProd


if [ $? -ne 0 ]; then
  echo "Gradle buildFrontEndProd did not complete successfully, not moving forward."
  exit 2;
fi

# parse version
BUILD_GRADLE_PATH="build.gradle"

# Find current version and only return matched string
VERSION_STRING=$(grep -oE "version.*=.*'[[:digit:]]+.[[:digit:]]+.[[:digit:]]+-SNAPSHOT'" $BUILD_GRADLE_PATH)

# parse current version
VERSION_PREFIX=$(echo "$VERSION_STRING" | cut -f1 -d= | tr -d '[:space:]')
CURRENT_VERSION=$(echo "$VERSION_STRING" | cut -f2 -d= | tr -d '[:space:]')
echo "Parsed existing version. Replacing with new version. [Current Version: ${CURRENT_VERSION}] [New Version: ${NEW_VERSION}]"

## Debug logs
#echo $VERSION_STRING
#echo $CURRENT_VERSION
#echo "${VERSION_PREFIX} = ${CURRENT_VERSION}"

NEW_VERSION_STRING="${VERSION_PREFIX} = '${NEW_VERSION}-SNAPSHOT'"

#replace version
sed -i "s/${VERSION_STRING}/${NEW_VERSION_STRING}/i" $BUILD_GRADLE_PATH



# Forcing resources to be added during this step
git add -f src/main/resources/META-INF
git commit -am "Checking in built artifacts for Prod; Upped version to ${NEW_VERSION}"
git push
git tag -a "v${NEW_VERSION}" -m "Release v${NEW_VERSION}"
git push origin "v${NEW_VERSION}"

