#
# Select base image
FROM openwhisk/ow-utils:63a5498

#
# Update
RUN apt-get update && apt-get upgrade -y

#
# Install software common packets and development tools to build Node native addons
RUN apt-get -y install curl dirmngr apt-transport-https lsb-release ca-certificates software-properties-common gcc g++ make sudo

#
# Install Node.js, NPM
RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
RUN apt-get update && apt-get install nodejs -y

#
# Install OpenWhisk composer
RUN npm --force install -g openwhisk-composer