{
  outputs = inputs: {
    packages.x86_64-linux.default = import inputs.self {system = "x86_64-linux";};
  };
}
