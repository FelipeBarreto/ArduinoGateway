package br.ufc.great.syssu.base3.utils;

import java.util.Map;

public interface IMappable<T> {
    Map<String, Object> getMap();
    T getObject();
}
