package ecat.model.ajax.catctrl


import ecat.model.Schema.Room
import ecat.model.ajax.catctrl.CategoryControlProtocol.RoomCtrlRequest

object MiscFunctions {


  //TODO: UNSHIT THIS!  (also Refactor keys to Any)
  def limits(data: List[List[Int]], input: Map[Int, List[Int]])= {//: Option[List[(List[Int], List[Int])]] = {


    val _data= data.iterator.zipWithIndex.map(_.swap).toMap


    //data ids->covering input ids
    def groupped: Map[Set[Int], Set[Int]] = input.foldLeft(Map.empty[Set[Int],Set[Int]]){case (agr,(id,points))=>
      val dps = _data.iterator.filter(_._2.iterator.zip(points.iterator).forall(t => t._1 >= t._2)).map(_._1).toSet
      agr + (dps -> agr.get(dps).fold(Set(id))(_ + id))
    }

    @inline
    def multiMax(t:Set[List[Int]])=t.reduce(_.zip(_).map(t=>t._1.max(t._2)))


    //this method calculates the limits of locked groups(ones where dataPoints size == input size). Returns limits and uncloked leftovers(subsequently covered by processOthers)
    //TODO: Refactor!
    def processLocked(m: Map[Set[Int], Set[Int]], locked: Set[Int] = Set(), limits: Map[Int, List[Int]] = Map()): (Map[Int, List[Int]], Map[Set[Int], Set[Int]]) ={
//
//      filter out locked data points and regroup data point
      val fltm= m.foldLeft(Map.empty[Set[Int], Set[Int]]){ case(agr,(dps,inps))=>
        val flt =  dps.filterNot(locked)
        agr + (flt-> agr.get(flt).fold(inps)(_ ++ inps))
      }
//
//      find element where input set is minimally covered with dataset
      fltm.find(t=> t._1.size == t._2.size)
//        return if no more minimally covering elements are found, otherwise update limits, locked elements and recurse
        .fold(limits->fltm){ case (dpIds,inpIds)=>
        processLocked(
          fltm-dpIds,
          locked ++ dpIds,
          limits ++ inpIds.map(_ -> multiMax(dpIds.map(_data(_))))
        )
      }
    }


    val (limits,dataToInps) = processLocked(groupped)
//    println(s"limits=$limits, dataToInps=$dataToInps")


    val inpToData: Map[Int, Set[Int]] = dataToInps.foldLeft(Map.empty[Int,Set[Int]]){case(agr,(dps,ips))=>
      ips.foldLeft(agr){case(_agr,ip)=>
        _agr + (ip -> _agr.get(ip).fold(dps)(_ ++ dps))
      }
    }

    val dataToInp: Map[Int, Set[Int]] = inpToData.foldLeft(Map.empty[Int,Set[Int]]){case(agr,(ip,dps))=>
      dps.foldLeft(agr){case(_agr,dp)=>
        _agr + (dp -> _agr.get(dp).fold(Set(ip))(_ + ip))
      }
    }

    def isCovered (inps:Set[Int],dti: Map[Int, Set[Int]],itd:Map[Int, Set[Int]]):Boolean={
      inps.isEmpty || (
      dti.nonEmpty && itd.forall(_._2.nonEmpty)&& {
        val minCoveredInpId = inps.minBy(i=>itd(i).size)
        val minCoveringDp = itd(minCoveredInpId).minBy(d=>dti(d).size)
        def newDti  = (dti - minCoveringDp).map{case (d,is)=> d-> (is - minCoveredInpId)}
        def newItd = (itd - minCoveredInpId).map{case(i,ds)=>i->(ds - minCoveringDp)}
        isCovered(inps - minCoveredInpId, newDti, newItd)
      })
    }

    if(isCovered(inpToData.map(_._1).toSet,dataToInp,inpToData)) Some(limits ++ inpToData.map{case(i,d)=>i-> multiMax(d.map(data))})
    else None


  }


}
