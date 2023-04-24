## Set up local computer running MacOS

MacOS is pretty easy operating system to prepare all the necessary tools. Here is what needs to be done:

- [Install ssh](#install-ssh)
- [Install rsync](#install-rsync)
- [Set up an ssh connection to the server](#set-up-an-ssh-connection-to-the-server)
- [Next steps](#next-steps)

# Install ssh

Since MacOS has a lot of command-line tools out of the box, you won't have much trouble with them. OpenSSH should already be available out of the box. Check that you have it installed by calling the ssh command with an argument to retrieve its version in your terminal. Like this:
```
$ ssh -V
OpenSSH_8.1p1, LibreSSL 2.7.3
```

It should be there, and if it is, then this step is done.

# Install rsync

There is a little catch here with rsync. You might have multiple versions of it on your operating system. As I mentioned above, MacOS provides a lot of command-line tools out of the box, and rsync will probably already be present in your system. However, the version provided by Apple can be significantly behind the latest one.

The solution to the problem is to use the rsync version from [**Brew**](https://brew.sh) - commandline package manager for MacOS.

Since you are a developer, Brew is most likely already installed on your computer, but if not, proceed to their website. On their front page, you will see the one-line command instruction that you should execute in your terminal to install Brew. The command should be like this:
```
$ /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

After Brew is installed on your computer, download and install the latest rsync version by running this in your terminal:
```
$ brew install rsync
```

After that, check out the version of rsync:
```
$ rsync --version
rsync version 3.2.7 protocol version 31
...
```

It should look like this. If you see something like <kbd>rsync version 2.6.9 protocol version 29</kbd> in response, then your system's rsync points to the Apple version of it, and you have to install the Brew version.

## Set up an ssh connection to the server

Now with all the binaries properly installed, let's set up the connection properly. By this time you should already have a prepared remote server, with properly configured openssh server. If not - check out [this instruction](./setup_remote.md).

First, the plugin **REQUIRES** you to connect to a remote server using public key authentication, and I highly recommend create a dedicated keypair for this, not using your normal one. So, if you haven't already created a key pair, let's do this. I recommend using the ED25519 algorithm because of its reliability and incredible performance. So, create a keypair with a command like this:
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
