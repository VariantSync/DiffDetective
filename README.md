
![Maven](https://github.com/VariantSync/DiffDetective/actions/workflows/maven.yml/badge.svg)
[![Documentation](https://img.shields.io/badge/Documentation-Read-purple)][documentation]
[![GitHubPages](https://img.shields.io/badge/GitHub%20Pages-online-blue.svg?style=flat)][website]
[![License](https://img.shields.io/badge/License-GNU%20LGPLv3-blue)](LICENSE.LGPL3)

# DiffDetective - Variability-Aware Source Code Differencing

DiffDetective is an open-source Java library for variability-aware source code differencing and the **analysis of version histories of software product lines**. This means that DiffDetective can **turn a generic differencer into a variability-aware differencer** by means of a pre- or post-processing. DiffDetective is centered around **formally verified** data structures for variability (variation trees) and variability-aware diffs (variation diffs). These data structures are **generic**, and DiffDetective currently implements **C preprocessor support** to parse respective annotations when used to implement variability. The picture below depicts the process of variability-aware differencing.

<img alt="Variability-Aware Differencing Overview" src="docs/teaser.png" height="500" />

Given two states of a C-preprocessor annotated source code file (left), for example before and after a commit, DiffDetective constructs a variability-aware diff (right) that distinguishes changes to source code from canges to variability annotations. DiffDetective can construct such a variation diff either, by first using a generic differencer, and separating the information (center path), or by first parsing both input versions to an abstract representation, a variation tree (center top and bottom), and constructing a variation diff using a tree differencing algorithm in a second step.

Additionally, DiffDetective offers a **flexible framework for large-scale empirical analyses of git version histories** of statically configurable software. In multiple studies, DiffDetective was successfully employed to study the commit histories of up to 44 open-source git repositories, including the **Linux Kernel, GCC, Vim, Emacs, or the Godot game engine**.

## Setup

DiffDetective is a Java Maven library. While DiffDetective depends on some custom libraries ([FeatureIDE library](https://featureide.github.io/), [Sat4j](https://sat4j.org/), [Functjonal](https://github.com/VariantSync/Functjonal)) these are prepackaged with DiffDetective. So **all you need is [Maven](https://maven.apache.org/)** installed.

DiffDetective also comes as a **nix package** (see [default.nix](default.nix)).

### Cloning the repository

First, clone this repository and navigate inside it:
```shell
git clone https://github.com/VariantSync/DiffDetective
cd DiffDetective
```

### Building and installing

You can build and install DiffDetective using Maven such that it can be used in your own `pom.xml`. Alternatively, you can use a jar which includes all necessary dependencies. Such a jar can either be built manually using Maven or using Nix.

#### Building and installing with Maven

For building and installing Maven needs to be installed. Either provide it yourselves (e.g., using the system package manager) or, if you have Nix installed, run `nix-shell` (stable Nix) or `nix develop` (Nix Flakes) to provide all necessary build tools.

Next, build DiffDetective and install it on your system so that you can access it from your own projects:
```shell
mvn install
```

DiffDetective is now available on your system. Add the following to the pom.xml of your Maven project to add DiffDetective as a dependency, but make sure to pick the right version number. You can find the version number of DiffDetective at the top of the pom.xml file of DiffDetective. 

```xml
<dependency>
    <groupId>org.variantsync</groupId>
    <artifactId>DiffDetective</artifactId>
    <version>2.0.0</version>
</dependency>
```

If you prefer to just use a jar file, you can find a jar file with all dependencies in `DiffDetective/target/diffdetective-2.0.0-jar-with-dependencies.jar` (again, the version number might be different).
You can (re-)produce this jar file by either running `mvn package` or `mvn install` within you local clone of DiffDetective.

> Disclaimer: Setup tested with maven version 3.6.3.

#### Building with Nix

Alternatively to manual building using Maven, Nix can be used. Both a [flake.nix](flake.nix) and a [default.nix](default.nix) are provided. Hence, you can build DiffDetective using
```shell
nix-build # stable version
# or
nix build # Flake version
```
In case you are using Nix Flakes, you can skip cloning the repository as usual: `nix build github:VariantSync/DiffDetective#.`

Afterwards, the [result](result) symlink points to the [Javadoc](result/share/github-pages/docs/javadoc/index.html), the [DiffDetective jar](result/share/java/DiffDetective/DiffDetective.jar) and a simple [script](result/bin/DiffDetective) for executing a DiffDetective main class provided as argument (e.g., evaluations used in previous research).

## Publications

### Classifying Edits to Variability in Source Code (ESEC/FSE 2022)

[![Preprint](https://img.shields.io/badge/Preprint-Read-purple)](https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf)
[![Paper](https://img.shields.io/badge/Paper-Read-purple)](https://dl.acm.org/doi/10.1145/3540250.3549108)
[![Talk](https://img.shields.io/badge/Talk-Watch-purple)](https://www.youtube.com/watch?v=EnDx1AWxD24)
[![Original Replication Package](https://img.shields.io/badge/Replication_Package-Original-blue)](https://github.com/VariantSync/DiffDetective/tree/esecfse22)
[![Updated Replication Package](https://img.shields.io/badge/Replication_Package-Updated-blue)](replication/esecfse22/README.md)
[![Artifact DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.7110095.svg)](https://doi.org/10.5281/zenodo.7110095)

> P. M. Bittner, C.Tinnes, A. Schultheiß, S. Viegener, T. Kehrer, T. Thüm. _Classifying Edits to Variability in Source Code_. In Proceedings of the 30th ACM Joint European Software Engineering Conference and Symposium on the Foundations of Software Engineering (ESEC/FSE 2022), ACM, New York, NY, November 2022

<img padding="10" align="right" src="https://www.acm.org/binaries/content/gallery/acm/publications/artifact-review-v1_1-badges/artifacts_evaluated_reusable_v1_1.png" alt="ACM Artifacts Evaluated Reusable" width="114" height="113"/>

This was the initial work, introducing DiffDetective as a means to conduct an empirical evaluation of a classification of edits.
In particular, we used DiffDetective to classify the effect of edits on the variability of the edited source code in the change histories of 44 open-source C-preprocessor-based software projects.

The classification is implemented within the [org.variantsync.diffdetective.editclass](src/main/java/org/variantsync/diffdetective/editclass/) package.

The original replication package can be found on the [esecfse](https://github.com/VariantSync/DiffDetective/tree/esecfse22) branch or via the DOI [10.5281/zenodo.7110095](https://doi.org/10.5281/zenodo.7110095). The replication is also available for the most recent version of DiffDetective with various improvements, which will likely yield to slightly different results than the initial study. The updated replication package can be found in the [replication/esecfse22](replication/esecfse22) subdirectory with it's own [README](replication/esecfse22/README.md).


### Views on Edits to Variational Software (SPLC 2023)

[![Preprint](https://img.shields.io/badge/Preprint-Read-purple)](https://github.com/SoftVarE-Group/Papers/raw/main/2023/2023-SPLC-Bittner.pdf)
[![Paper](https://img.shields.io/badge/Paper-Read-purple)](https://dl.acm.org/doi/10.1145/3579027.3608985)
[![Original Replication Package](https://img.shields.io/badge/Replication_Package-Original-blue)](https://github.com/VariantSync/DiffDetective/tree/splc23-views/replication/splc23-views)
[![Updated Replication Package](https://img.shields.io/badge/Replication_Package-Updated-blue)](replication/splc23-views/README.md)
[![Artifact DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8027920.svg)](https://doi.org/10.5281/zenodo.8027920)

> P. M. Bittner, A. Schultheiß, S. Greiner, B. Moosherr, S. Krieter, C. Tinnes, T. Kehrer, T. Thüm. _Views on Edits to Variational Software_. In Proceedings of the 27th ACM International Systems and Software Product Line Conference (SPLC 2023), ACM, New York, NY, August 2023

<img padding="10" align="right" src="https://www.acm.org/binaries/content/gallery/acm/publications/artifact-review-v1_1-badges/artifacts_evaluated_functional_v1_1.png" alt="ACM Artifacts Evaluated Reusable" width="114" height="113"/>

In this work, we used DiffDetective for a feasibility study of creating views on edits to C-preprocessor based software.
The idea of a view is to act as a filter on relevant parts of a system.
For instance, a piece of source code may be deemed relevant if it implements a certain feature.
A view on an edit thus is a simplified form of an edit that, for example, contains only changes to a certain feature.
From a mathematical perspective, creating such views is in fact a lifting of operations on single revisions of variational systems to operations on diffs of variational systems.

Views are implemented within the [org.variantsync.diffdetective.variation.tree.view](src/main/java/org/variantsync/diffdetective/variation/tree/view/) and [org.variantsync.diffdetective.variation.diff.view](src/main/java/org/variantsync/diffdetective/variation/diff/view/) packages for variaton trees and diffs, respectively.

The original replication package can be found on the `splc23-views` branch within the directory [replication/splc23-views](https://github.com/VariantSync/DiffDetective/tree/splc23-views/replication/splc23-views) or via the DOI [10.5281/zenodo.8027920](https://doi.org/10.5281/zenodo.8027920). The replication is also available for the most recent version of DiffDetective with an up-to-date version of DiffDetective, which will likely yield to slightly different results than the initial study. The updated replication package can be found in the [replication/splc23-views](replication/splc23-views) subdirectory with it's own [README](replication/splc23-views/README.md).


### Explaining Edits to Variability Annotations in Evolving Software Product Lines (VaMoS 2024)

[![Preprint](https://img.shields.io/badge/Preprint-Read-purple)](https://github.com/SoftVarE-Group/Papers/raw/main/2024/2024-VaMoS-Guething.pdf)
[![Paper](https://img.shields.io/badge/Paper-Read-purple)](https://doi.org/10.1145/3634713.3634725)
[![Replication Package](https://img.shields.io/badge/Replication_Package-at_Fork-blue)][forklg]
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.10286851.svg)](https://doi.org/10.5281/zenodo.10286851)

> L. Güthing, P. M. Bittner, I. Schaefer, T. Thüm. _Explaining Edits to Variability Annotations in Evolving Software Product Lines_. In Proceedings of the 18th International Working Conference on Variability Modelling of Software-Intensive Systems (VaMoS 2024), ACM, New York, NY, February 2024

<img padding="10" align="right" src="https://www.acm.org/binaries/content/gallery/acm/publications/artifact-review-v1_1-badges/artifacts_evaluated_functional_v1_1.png" alt="ACM Artifacts Evaluated Reusable" width="114" height="113"/>

In this work, we formalized an extension of variation diffs, with a typing for edges and pair-wise relations for variability annotations (i.e., mapping nodes in variation diffs). Such edge-typed variation diffs show for example that two annotations exlude or imply each other. Such edge-typed diffs might help better explaining or analyzing edits in the future.

Edge-typed variation diffs and the replication package are implemented in a fork of DiffDetective ([https://github.com/guethilu/DiffDetective][forklg]). The replication package is archived under the DOI [10.5281/zenodo.10286851](https://doi.org/10.5281/zenodo.10286851).

### Student Theses

DiffDetective was extended and used within bachelor's and master's theses:

- _Constructing Variation Diffs Using Tree Diffing Algorithms_, Benjamin Moosherr, Bachelor's Thesis, 2023, [DOI 10.18725/OPARU-50108](https://dx.doi.org/10.18725/OPARU-50108): Benjamin added support for tree-differencing and integrated the GumTree differencer ([Github](https://github.com/GumTreeDiff/gumtree), [Paper](https://doi.org/10.1145/2642937.2642982)). In his thesis, Benjamin also reviewed a range of quality metrics for tree-diffs with focus on their applicability for rating variability-aware diffs.
- _Reverse Engineering Feature-Aware Commits From Software Product-Line Repositories_, Lukas Bormann, Bachelor's Thesis, 2023, [10.18725/OPARU-47892](https://dx.doi.org/10.18725/OPARU-47892): Lukas implemented an algorithm for feature-based commit-untangling, which turns variation diff into a series of smaller diffs, each of which contains an edit to a single feature or feature formula. This work was later refined in our publication _Views on Edits to Variational Software_ illustrated above.
- _Inspecting the Evolution of Feature Annotations in Configurable Software_, Lukas Güthing, Master's Thesis, 2023: Lukas implemented different edge-types for associating variability annotations within variation diffs. He published his work later at VaMoS 2024 under the title _Explaining Edits to Variability Annotations in Evolving Software Product Lines_, illustrated above.
- _Empirical Evaluation of Feature Trace Recording on the Edit History of Marlin_, Sören Viegener, Bachelor's Thesis, 2021, [DOI 10.18725/OPARU-38603](http://dx.doi.org/10.18725/OPARU-38603): In his thesis, Sören started the DiffDetective project and implemented the first version of an algorithm, which parses text-based diffs to C-preprocessor files to variation diffs. He also came up with an initial classification of edits, which we wanted to reuse to evaluate [Feature Trace Recording](https://variantsync.github.io/FeatureTraceRecording/), a method for deriving variability annotations from annotated patches.

[documentation]: https://htmlpreview.github.io/?https://github.com/VariantSync/DiffDetective/blob/splc23-views/docs/javadoc/index.html
[website]: https://variantsync.github.io/DiffDetective/
[forklg]: https://github.com/guethilu/DiffDetective