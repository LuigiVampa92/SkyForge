## Set up remote server

In order to make remote Gradle builds of your Android projects, you have to properly prepare the server. The plugin can do most of the job of preparing the dependencies itself, however, some actions you will have to perform manually, like basic settings: users, ssh server configuration, etc.

For now, only the instructions for Ubuntu 20+ are presented. Technically, you can use any Linux distro, but the Plugin's feature to automatically prepare all the dependencies on a remote server currently supports only Ubuntu 20+. Ubuntu is picked as the easiest to setup distro, which will be beneficial for beginners. If you use another distro, I'm pretty sure you won't need any of my instructions and can easily set up everything just by taking this instruction as a reference.

Check out the detailed instructions for your operating system:

- [Ubuntu 20+](./setup_remote_ubuntu_20.md)