## Set up a remote server running Ubuntu 20+

Setting up a server is not very complicated. Your "Server" can be, for example, your personal desktop computer that you have ssh access to across the network, or a brand new virtual machine (VM) or virtual private server (VPS). Technically, all you have to do is:
- [Prepare system user](#prepare-system-user)
- [Set timezone](#set-timezone)
- [Setup ssh access](#setup-ssh-access)

## Prepare system user

While you can use <kbd>root</kbd> user without any problems, it is always wise to set up a dedicated user for tasks like this. Here are the example commands to create a new user, set a password, default shell, and make sure it is in the <kbd>sudo</kbd> group. In my case, it will be <kbd>builder:builder</kbd>:
```
useradd -m builder
echo "builder:builder" | chpasswd
usermod -aG sudo builder
chsh -s /bin/bash builder
```

## Set timezone

Set a desired timezone (**put your value**):
```
timedatectl set-timezone Europe/London
```

## Setup ssh access

Now prepare your ed25519 ssh public key value (copy it to the clipboard, for example) that will be stored in the authorized keys file for this user on the server. If you do not have it yet, check out [these instructions to set up your local computer](./setup_local.md)

Let's create a <kbd>.ssh</kbd> folder, give this folder the right permissions, and save the public key to be able to connect and authenticate as this user (**replace** <kbd>___YOUR_PUBLIC_KEY_VALUE___</kbd> **with an actual value**):
```
mkdir -p /home/builder/.ssh
echo ___YOUR_PUBLIC_KEY_VALUE___ >> /home/builder/.ssh/authorized_keys
chown -R builder:builder /home/builder/.ssh
chmod 700 /home/builder/.ssh
chmod 600 /home/builder/.ssh/*
```

Now, edit the openssh server config file <kbd>/etc/ssh/sshd_config</kbd>. Here is a simple universal example of config file that will work perfectly for both dedicated user and default root user connections with public keys authentication only:
```
AddressFamily inet
ListenAddress 0.0.0.0
Port 34567
HostKey /etc/ssh/ssh_host_ed25519_key
SyslogFacility AUTH
LogLevel INFO
Protocol 2
HostKeyAlgorithms ssh-ed25519,ssh-ed25519-cert-v01@openssh.com
KexAlgorithms curve25519-sha256,curve25519-sha256@libssh.org
Ciphers chacha20-poly1305@openssh.com,aes128-gcm@openssh.com,aes256-gcm@openssh.com,aes128-ctr,aes192-ctr,aes256-ctr
MACs hmac-sha2-256-etm@openssh.com,hmac-sha2-512-etm@openssh.com
LoginGraceTime 45
PermitRootLogin yes
StrictModes yes
MaxAuthTries 5
MaxSessions 5
MaxStartups 2:50:10
PubkeyAuthentication yes
PasswordAuthentication no
PermitEmptyPasswords no
ChallengeResponseAuthentication no
UsePAM no
X11Forwarding no
PrintMotd no
GatewayPorts yes
Compression yes
AcceptEnv LANG LC_*
Subsystem sftp /usr/lib/openssh/sftp-server
```

Pretty simple setup. Note that the port is changed to a non-standard value <kbd>34567</kbd> to avoid unnecessary bruteforce bot connections. The fastest and securest algorithms are chosen. Only <kbd>ED25519</kbd> keys are left for the sake of performance. GatewayPorts are enabled in case your project will require the usage of reverse ssh tunnels.

Check that the config contains no errors and will work after restarting the openssh server:
```
$ sshd -t
```
This command should show no output if there are no errors. If that is so, restart the ssh server:
```
$ systemctl restart sshd
```
## Check connection

On your local computer, open the terminal. You should be able to connect to the server by using an alias. Try this:
```
$ ssh android_builds_server
```

You should successfully connect to the server.

## Next steps

Congrateulations. Everything is set up and prepared. Now the Plugin will be able to connect to the remote server. Everything else: installing system dependencies, JDK, Android SDK, cmdline-tools, platform-tools, build-tools etc. will be done by the Plugin itself. You do **NOT** have to do it manually. Your remote server is configured. Now proceed to [plugin usage instructions](./usage_general.md)
