package ru.router.chain;

import ru.router.model.Fix;

public interface Chain {

    void handle(Fix message);

    Chain getNext();

    void setNext(Chain next);
}
