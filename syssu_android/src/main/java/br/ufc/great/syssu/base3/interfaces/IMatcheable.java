package br.ufc.great.syssu.base3.interfaces;

import br.ufc.great.syssu.base3.FilterException;
import br.ufc.great.syssu.base3.Query;

public interface IMatcheable {
    boolean matches(Query query) throws FilterException ;
}
