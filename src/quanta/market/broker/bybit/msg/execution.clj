(ns quanta.market.broker.bybit.msg.execution)


{:creationTime 1721702600937, :topic "execution", 
 :id "102782796_20000_1452874840", 
 :data [{; order 
         :symbol "BTCUSDT",
         :category "spot", 
         :side "Buy", 
         :orderQty "0.001000", 
         :orderType "Limit", 
         :orderPrice "68750.00", 
         :stopOrderType "",
         :marketUnit "",
         :isLeverage "0",
         :markPrice "",
         :tradeIv "",
         :indexPrice "", 
         :underlyingPrice "", 
         :markIv "", 
         :orderLinkId "31k32-8-",
         :orderId "1736161684319725568", 
         :isMaker false, 
         :seq 1452874840, 
         ; orderstatus
         :leavesQty "0.000000" 
         :closedSize "", 
         ; execution
         :execId "2100000000079525951", 
         :execType "Trade", 
         :execQty "0.001000", 
         :execPrice "68087.03", 
         :blockTradeId "",          
         :execFee "0.000001",
         :execTime "1721702600932", 
         :execValue "68.08703000", 
         :feeRate "0.001"}]}


(def bybit-exec-types
  ["Trade"
   "AdlTrade" ;Auto-Deleveraging
   "Funding" ;Funding fee
   "BustTrade" ;Takeover liquidation
   "Delivery" ;USDC futures delivery; Position closed by contract delisted
   "Settle" ; Inverse futures settlement; Position closed due to delisting
   "BlockTrade"
   "MovePosition"
   "UNKNOWN"])

{:creationTime 1721703999518, 
 :topic "execution", 
 :id "102782796_BTC-27SEP24_17029046221", 
 :data [{:underlyingPrice "", 
         :category "linear", 
         :execType "Trade", 
         :execPrice "69175", 
         :orderQty "0.001", 
         :closedSize "0", 
         :orderPrice "72420.5", 
         :stopOrderType "UNKNOWN", 
         :symbol "BTC-27SEP24", 
         :orderType "Market", 
         :marketUnit "", :isLeverage "0", :markPrice "68989.88", 
         :tradeIv "", :indexPrice "", :createType "CreateByUser", 
         :leavesQty "0", :markIv "", :blockTradeId "", 
         :execQty "0.001", :execId "60c80c8e-ed4c-5505-9135-743b457d22ee", 
         :execFee "0.03804625", :execTime "1721703999514", :side "Buy", 
         :isMaker false, :seq 17029046221, :execValue "69.175", 
         :orderLinkId "MjARTou9", :orderId "9c38c328-30cf-4374-9d69-9d30d3817201",
         :feeRate "0.00055"}]}
