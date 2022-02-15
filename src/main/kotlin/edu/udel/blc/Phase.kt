package edu.udel.blc

import java.util.function.Function

interface Phase<T, R> : Function<T, Result<R>>