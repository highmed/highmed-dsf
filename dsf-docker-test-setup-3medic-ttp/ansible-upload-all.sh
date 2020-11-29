#!/bin/bash

cd ttp
ansible-playbook upload.yml
cd ../medic1
ansible-playbook upload.yml
cd ../medic2
ansible-playbook upload.yml
cd ../medic3
ansible-playbook upload.yml
cd ..
