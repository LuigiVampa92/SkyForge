## Set up local computer running Linux

Linux is very easy operating system to prepare all the necessary tools. You probably already have everything set up, but just in case, let's run through all the steps. Here is what needs to be done:

- [Install ssh](#install-ssh)
- [Install rsync](#install-rsync)
- [Set up an ssh connection to the server](#set-up-an-ssh-connection-to-the-server)
- [Next steps](#next-steps)

All the example commands here are provided for Ubuntu 20+, but if you run some other distro, like Fedora or Arch(btw), then all the difference will just be in the package manager syntax.

# Install ssh

Since Linux distros have most of the command-line tools out of the box, you won't have much trouble with them. OpenSSH should already be available out of the box. Check that you have it installed by calling the ssh command with an argument to retrieve its version in your terminal. Like this:
```
$ ssh -V
OpenSSH_8.1p1, LibreSSL 2.7.3
```

It should be there, and if it is, then this step is done.

# Install rsync

Install rsync using the package manager for your distro if you don't have it already. Run in the terminal:
```
$ sudo apt install rsync -y
```

After it's done, check that the rsync binary runs properly and that the version of rsync is the latest one:
```
$ rsync --version
rsync version 3.2.7 protocol version 31
...
```

## Set up an ssh connection to the server

Now with all the binaries are properly installed, let's set up the connection properly. By this time, you should already have a prepared remote server with a properly configured openssh server. If not, check out [this instruction](./setup_remote.md).

First, the plugin **REQUIRES** you to connect to a remote server using public key authentication, and I highly recommend creating a dedicated key pair for this instead of using your normal one. So, if you haven't already created a key pair, let's do this. I recommend using the ED25519 algorithm because of its reliability and incredible performance. So, create a keypair with a command like this:
```
$ ssh-keygen -o -t ed25519 -f /home/pavel/.ssh/id_ed25519_android_builds_server -C "key_for_android_build_server" -P ""
```

**Note that you should insert your username instead of mine**, of course. And if you do not have <kbd>~/.ssh</kbd> folder for some reason, you should create it (<kbd>mkdir -p ~/.ssh</kbd>) before running the command above.

Now, after keys have been created, upload your public key (<kbd>cat ~/.ssh/id_ed25519_android_builds_server.pub</kbd>) onto the server. It should be added to the file <kbd>~/.ssh/authorized_keys</kbd> on your server.

Now, you should create a connection configuration with an alias. It should be added to the file <kbd>~/.ssh/config</kbd> on your local computer. If you do not yet have that file, then create it first: <kbd>touch ~/.ssh/config</kbd>, if you do, just append the record to the existing file contents and save the file. The record should look like this (**put your IP address instead of the one from the example below**):
```
Host android_builds_server
HostName 12.34.56.78
Port 34567
User builder
IdentityFile ~/.ssh/id_ed25519_android_builds_server
IdentitiesOnly yes
Compression yes
```

After that, close the terminal and reopen it again to apply the changes. You should be able to connect to the server using an alias. Try this:
```
$ ssh android_builds_server
```

You should successfully connect to the server.

## Next steps

Congratulations! Everything is done. Your local computer is configured. Now proceed to [plugin usage instructions](./usage_general.md)
