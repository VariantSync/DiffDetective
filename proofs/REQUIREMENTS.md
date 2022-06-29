## Hardware Requirements
None

## Software Requirements

The [Stack build system][stack] is the only requirement to build our Haskell library and to run its example.
Stack is the de-facto standard for Haskell projects.
Dependencies to other packages are documented in the build files ([stack.yaml](stack.yaml) and [package.yaml](package.yaml)) and are handled automatically by Stack.
We do not require specific operating systems or other environments apart from Stack.
We successfully tested building the library and running the library on Windows 10 and in WSL2.

[stack]: https://docs.haskellstack.org/en/stable/README/
