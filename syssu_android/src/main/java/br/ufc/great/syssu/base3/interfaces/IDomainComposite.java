package br.ufc.great.syssu.base3.interfaces;

import br.ufc.great.syssu.base3.TupleSpaceException;

public interface IDomainComposite {
    IDomain getDomain(String name) throws TupleSpaceException;
}
