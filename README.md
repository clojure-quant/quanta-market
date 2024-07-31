# quanta-market [![GitHub Actions status |clojure-quant/quanta-market](https://github.com/clojure-quant/quanta-market/workflows/CI/badge.svg)](https://github.com/clojure-quant/quanta-market/actions?workflow=CI)[![Clojars Project](https://img.shields.io/clojars/v/io.github.clojure-quant/quanta-market.svg)](https://clojars.org/io.github.clojure-quant/quanta-market)

Quanta-Market is a library that allows to iteract with the market.
- quanta.quote can gets realtime quote-flows from different exchanges.
  Current Implementations are
  - bybit (trade, orderbook, statistics, bars, liquidations)
  - random (random quotes generated for testing)
- calendars (exchange and interval)
- trade routines

# demo

The demo folder contains various .clj files that can be evaluated in 
the repl. 

# unit tests

```
clj -M:test
```

