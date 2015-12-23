package br.ufc.great.syssu.servicemanagement.services;

import java.util.ArrayList;
import java.util.List;

import br.ufc.great.syssu.base3.Pattern;
import br.ufc.great.syssu.base3.Scope;
import br.ufc.great.syssu.base3.Tuple;
import br.ufc.great.syssu.base3.TupleSpaceException;
import br.ufc.great.syssu.base3.TupleSpaceSecurityException;
import br.ufc.great.syssu.base3.interfaces.IDomain;

public class TakeOneSyncService extends AbstractQueryService {

    @Override
    public String getName() {
        return "takeonesync";
    }

	@Override
	protected List<Tuple> query(IDomain domain, Pattern pattern, String restriction, String key, long timeout, Scope scope)
		throws TupleSpaceException, TupleSpaceSecurityException {
		List<Tuple> tuples = new ArrayList<Tuple>();
        tuples.add(domain.takeOneSync(pattern, restriction, key, timeout));
        return tuples;
	}
}
