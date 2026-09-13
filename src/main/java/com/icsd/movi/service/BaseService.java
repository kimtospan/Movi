package com.icsd.movi.service;

public interface BaseService<T, ID> {
    T getById(ID id);
}
