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

