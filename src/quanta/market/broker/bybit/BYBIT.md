

# Unified Account
1 wallet for spot/future/option


# Assets
   
category  #    collateral ordersize position
spot      596  usdt                 net long. long only
linear    432  usdt       min-vol   net
inverse   13   btc                  net (or long+short - configured in gui)
option    500

  (count (get-assets "spot"))    ;; => 596
  (count (get-assets "linear"))  ;; => 432
  (count (get-assets "inverse")) ;; => 13
  (count (get-assets "option"))  ;; => 500


# rate limits
              inverse	  linear	option	spot
order/create	10/s	    10/s	  10/s	  20/s


# BUY MARKET HACK
 - for buy market bybit interprets the qty in USD.
 - set "marketUnit" "baseCoin" ; hack for market buy orders so that qty is interpreted in BTC.