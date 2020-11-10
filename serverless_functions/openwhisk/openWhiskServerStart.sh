#!/bin/bash

sudo su
cd ~/openwhisk
(cd tools/ubuntu-setup && ./all.sh)
cd ~/openwhisk/ansible
ansible-playbook setup.yml
ansible-playbook prereq.yml
cd ~/openwhisk
./gradlew distDocker
cd ~/openwhisk/ansible
ansible-playbook initdb.yml
ansible-playbook wipe.yml
ansible-playbook openwhisk.yml
ansible-playbook postdeploy.yml
ansible-playbook -i environments/local openwhisk.yml
cd ~/openwhisk/bin
export PATH=$PATH:$PWD
cd ~/openwhisk