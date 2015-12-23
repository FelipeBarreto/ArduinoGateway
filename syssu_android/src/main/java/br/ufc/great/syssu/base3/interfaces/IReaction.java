package br.ufc.great.syssu.base3.interfaces;

import br.ufc.great.syssu.base3.Pattern;
import br.ufc.great.syssu.base3.Tuple;

public interface IReaction {

	void setId(Object id);

	Object getId();

	Pattern getPattern();
	
	String getRestriction();

	void react(Tuple tuple);
}
