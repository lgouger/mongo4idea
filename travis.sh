#!/usr/bin/env bash

cd ./idea-IC
export IDEA_HOME=$(pwd)
cd ..

# Run the tests
xvfb-run -a mvn clean install

# Was our build successful?
stat=$?

if [ "${TRAVIS}" != true ]; then
    mvn clean
    rm -rf idea-IC
fi

# Return the build status
exit ${stat}
