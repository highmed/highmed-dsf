param (
    [ValidateScript({
            if( -Not ($_ | Test-Path) ){
                throw "File or folder does not exist"
            }
            return $true
        })]
    [System.IO.FileInfo] $vhdParentPath = (Get-VMHost).VirtualHardDiskPath,
    [ValidateScript({
            if(-Not ($_ | Test-Path) ){
                throw "File or folder does not exist" 
            }
            if(-Not ($_ | Test-Path -PathType Leaf) ){
                throw "The Path argument must be a file."
            }
            return $true
        })]
    [System.IO.FileInfo] $ubuntuIsoPath = (Join-Path $HOME "Downloads\ubuntu-18.04.2-live-server-amd64.iso")
)

If (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {   
	$arguments = "& '" + $myinvocation.mycommand.definition + "'"
	Start-Process powershell -Verb runAs -ArgumentList $arguments
	Break
}

$vmSwitchName = 'HiGHmed'
$vms = @(@{name="HiGHmed_DockerRegistry";maxRam=1GB}, @{name="HiGHmed_TTP";maxRam=2GB}, @{name="HiGHmed_MeDIC_1";maxRam=2GB}, @{name="HiGHmed_MeDIC_2";maxRam=2GB}, @{name="HiGHmed_MeDIC_3";maxRam=2GB});

Write-Progress -Activity HiGHmed -Status 'Progress' -PercentComplete (100/($vms.Length+1)) -CurrentOperation "Configuring Network ..."
New-VMSwitch -name $vmSwitchName -SwitchType Internal
New-NetIPAddress -InterfaceAlias ('vEthernet (' + $vmSwitchName + ')') -IPAddress 10.42.0.1 -PrefixLength 24
New-NetNat -Name ($vmSwitchName + 'NAT') -InternalIPInterfaceAddressPrefix 10.42.0.0/24

for ($i=0; $i -lt $vms.Length; $i++)
{
    $vm = $vms[$i];
    $vhdPath = Join-Path $vhdParentPath ($vm.name + '.vhdx');
   
    Write-Progress -Activity HiGHmed -Status 'Progress' -PercentComplete (($i+2)*(100/($vms.Length+1))) -CurrentOperation "Creating VM $($vm.name) ..."
    New-VM -Name $vm.name -MemoryStartupBytes 512MB -NewVHDPath $vhdPath -NewVHDSizeBytes 16GB -SwitchName $vmSwitchName -Generation 2;
    Set-VMProcessor -VMName $vm.name -Count 2;
    Add-VMDvdDrive -VMName $vm.name -Path $ubuntuIsoPath;
    Set-VMFirmware -VMName $vm.name -BootOrder $(Get-VMDvdDrive -VMName $vm.name),$(Get-VMHardDiskDrive -VMName $vm.name),$(Get-VMNetworkAdapter -VMName $vm.name) -SecureBootTemplate MicrosoftUEFICertificateAuthority
    Set-VMMemory -VMName $vm.name -MaximumBytes $vm.maxRam
}