package mining.tablegen;

import mining.DiffTreeMiningResult;
import mining.dataset.MiningDataset;

public record Row(
        MiningDataset dataset,
        DiffTreeMiningResult results
) {}
