package services

import concurrent.duration._

package object auxiliaries {
  type Address = String
  type Jobcoin = BigDecimal
  
  def currentTime = System.currentTimeMillis().millis  
}