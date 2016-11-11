package ecat.wiring

import ecat.dal.BlockDal

trait BlockDalModule { self: ProxyModule =>
  lazy val blockDal = new BlockDal(proxy)
}
