package ecat.model.ajax.catctrl

import ecat.model.Schema.Room
import ecat.model.ajax.catctrl.CategoryControlProtocol.RoomCtrlRequest

object MiscFunctions {


  //find better name for this// TODO: Refactor keys to Any
  def limits(data: List[List[Int]], input: Map[Int, List[Int]]): Option[Map[Int, List[Int]]] = {

    val _data: Map[Int, List[Int]] = data.iterator.zipWithIndex.map(_.swap).toMap

    // traverse it with option, if no data el cover inp el - return none
    //data ids->covering input ids
    def groupped: Map[Set[Int], Set[Int]] = input.foldLeft(Map.empty[Set[Int],Set[Int]]){case (agr,(id,points))=>
      val dps = _data.iterator.filter(_._2.iterator.zip(points.iterator).forall(t => t._1 >= t._2)).map(_._1).toSet
      agr + (dps -> agr.get(dps).fold(Set(id))(_ + id))
    }

    @inline
    def multiMax(t:Set[List[Int]])=t.reduce(_.zip(_).map(t=>t._1.max(t._2)))


    //this method calculates the limits of locked groups(ones where dataPoints size == input size). Returns limits and uncloked leftovers(subsequently covered by processOthers)
    def processLocked(m: Map[Set[Int], Set[Int]], locked: Set[Int] = Set(), limits: Map[Int, List[Int]] = Map()): (Map[Int, List[Int]], Map[Set[Int], Set[Int]]) ={

      //filter out locked data points and regroup data point
      val fltm= m.foldLeft(Map.empty[Set[Int], Set[Int]]){ case(agr,(dps,inps))=>
        val flt =  dps.filterNot(locked)
        agr + (flt-> agr.get(flt).fold(inps)(_ ++ inps))
      }

      //find element where input set is minimally covered with dataset
      fltm.find(t=> t._1.size == t._2.size)
        //return if no more minimally covering elements are found, otherwise update limits, locked elements and recurse
        .fold(limits->fltm){ case (dpIds,inpIds)=>
        processLocked(
          fltm-dpIds,
          locked ++ dpIds,
          limits ++ inpIds.map(_ -> multiMax(dpIds.map(_data(_))))
        )
      }
    }

    //this method calculates the limits of non- locked groups(ones where dataPoints size > input size)
    //m - coverage info sorted by  the size of covered group()
    //small groups are covered first bcs otherwise big groops(cvoered by bigger group of dadaPoints) may pick dataPoints of smaller groups leaving off spare elements that are not fit for smaller groups.
    def processOthers(m: Seq[(Set[Int], Set[Int])], limits: Map[Int, List[Int]], usedDpIds: Set[Int]=Set()): Option[(Map[Int, List[Int]])] = {
      if (m.isEmpty) Some(limits)
      else {
        val (dpIds, inpIds) = m.head
        assert(dpIds.size != inpIds.size)
        val fltDpIds: Set[Int] = dpIds.filterNot(usedDpIds)
        if (fltDpIds.size < inpIds.size) None
        else {
          processOthers(
            m.tail,     //make dpIds a list
            limits ++ inpIds.map(_ -> multiMax(dpIds.map(_data(_)))),
            usedDpIds ++ fltDpIds.toList.map(id => id -> _data(id).reduce(_ * _)).sortBy(_._2).take(inpIds.size).map(_._1)
          )
        }
      }
    }

    val (limits,mp) = processLocked(groupped)
    //  println(s"limits=$limits, mp=$mp")
    processOthers(mp.toSeq.sortBy(_._2.size),limits)
  }


}
