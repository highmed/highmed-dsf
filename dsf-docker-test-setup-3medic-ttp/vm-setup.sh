#!/bin/bash

# Variables
## Folder where the VMs should be installed
Folder=~/Projects/highmed/vm-test-setup
## Path and filename of the ISO to be used
IsoPath=~/Downloads/ubuntu-18.04.3-live-server-amd64.iso
## Names of the VMs
VmNames=(HiGHmed_DockerRegistry HiGHmed_TTP HiGHmed_MeDIC_1 HiGHmed_MeDIC_2 HiGHmed_MeDIC_3)

echo ""
echo "Create hostonly interface ..."
HostOnlyInterfaceName=$(VBoxManage hostonlyif create | grep -o 'vboxnet\d')
echo "Created hostonly interface with name='$HostOnlyInterfaceName'"

echo "Set ip of hostonly interface ..."
VBoxManage hostonlyif ipconfig $HostOnlyInterfaceName --ip 10.42.0.1

for VmName in "${VmNames[@]}"
do
	echo ""
	echo "Initialize VM with name $VmName ..."
	VBoxManage createvm --name $VmName --ostype Ubuntu_64 --basefolder $Folder --register

	echo "Set memory ..."
	VBoxManage modifyvm $VmName --memory 2048

	echo "Create and add HD ..."
	VBoxManage createhd --filename $Folder/$VmName/$VmName.vdi --size 16000 --format VDI

	VBoxManage storagectl $VmName --name "SATA Controller" --add sata --controller IntelAhci
	VBoxManage storageattach $VmName --storagectl "SATA Controller" --port 0 --device 0 --type hdd --medium $Folder/$VmName/$VmName.vdi

	VBoxManage storagectl $VmName  --name "IDE Controller" --add ide --controller PIIX4
	VBoxManage storageattach $VmName  --storagectl "IDE Controller" --port 1 --device 0 --type dvddrive --medium $IsoPath

	echo "Add hostonly network adapter ..."
	vboxmanage modifyvm $VmName --nic2 hostonly --hostonlyadapter2 $HostOnlyInterfaceName

	echo "Initialize VM with name $VmName [DONE]" 
	echo ""   
done

echo "All VMs initialized"