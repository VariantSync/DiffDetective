package mining.tablegen.styles;

import mining.tablegen.rows.*;
import mining.tablegen.Row;
import mining.tablegen.TableDefinition;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static mining.tablegen.Alignment.*;
import static mining.tablegen.Alignment.RIGHT;

public class ShortTable extends TableDefinition {

    protected ShortTable() {
        super(new Table1().columnDefinitions());
        this.columnDefinitions.removeIf(c -> c.header().equals(ProposedAtomicPatterns.Untouched.getName()));
    }

    @Override
    public List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult) {
        final Comparator<ContentRow> larger = (a, b) -> -Integer.compare(a.results().totalCommits, b.results().totalCommits);
        final List<Row> res = rows.stream()
                .sorted(larger)
                .limit(4)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        res.add(new HLine());

        rows.stream()
                .filter(m -> m.dataset().name().equalsIgnoreCase("Marlin")
                        || m.dataset().name().equalsIgnoreCase("libssh")
                        || m.dataset().name().equalsIgnoreCase("Busybox")
                        || m.dataset().name().equalsIgnoreCase("Godot"))
                .sorted(larger)
                .forEach(res::add);

        res.add(new HLine());
        res.add(new DummyRow());
        res.add(new HLine());
        res.add(new HLine());
        res.add(ultimateResult);

        return res;
    }
}
