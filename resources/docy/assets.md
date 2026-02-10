# assets


:type 
:equity :etf :fx :future :crypto
:bond :fund :index

## bybit

- spot 30 paare symbol btcusdt sollte eindeutig sein.
- future 30 paare (nur continuous=perpetual) btcusdt0, endung immer mit 0

## kibot
- spot-fx "EUR/USD" immer mit schraegstrich, das vermeidet konflikte
- future - nur continuous futures
  symbol endet immer mit 0, das heisst continuous und vermeidet konflikte
- etf sind nur 5 stellen, kein conflikt mit bybit.


## normalized edn asset 
in resources: asset/
- bybit-spot.edn 
- bybit-future.edn 
- kibot-forex.edn 
- kibot-future.edn 
- kibot-etf.edn

die idee ist, dass mehrere edn files geladen werden koennen,
und wenn 2 zeilen das selbe :asset haben, dass wir dann den eintrag mergen.

:asset unser symbol
:category :spot :future :crypto-spot :crypto-future :etf
:name der name des symbols
:calendar also :us :crypto :forex
:kibot symbol von kibot
:bybit symbol von bybit
:bybit-category (brauchen wir nicht, kann aus category errechnet werden)
:kibot-category (brauchen wir nicht, kann aus category errechnet werden)
-kibot-link-d
-kibot-link-m