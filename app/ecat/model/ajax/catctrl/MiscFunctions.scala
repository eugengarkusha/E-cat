package ecat.model.ajax.catctrl


import ecat.model.Schema.Room
import ecat.model.ajax.catctrl.CategoryControlProtocol.RoomCtrlRequest

object MiscFunctions {


  // TODO: Change input keys type to Any
  def limits(data: List[List[Int]], input: Map[Int, List[Int]]): Option[Map[Int, List[Int]]] = {

    val _data: Map[Int, List[Int]] = data.iterator.zipWithIndex.map(_.swap).toMap

    val _inpIds:List[Int] = input.map(_._1)(collection.breakOut)

    @inline
    def multiMax(t: Set[List[Int]]) = t.reduce(_.zip(_).map(t => t._1.max(t._2)))

    //index
    val inpToData: Map[Int, Set[Int]] = input.map { case (iid, inputPoint) =>
      iid -> _data.iterator.filter{ case (did, dataPoint) =>
        dataPoint.zip(inputPoint).forall(t => t._1 >= t._2)
      }.map(_._1).toSet
    }(collection.breakOut)

    //inverted index
    val dataToInp: Map[Int, Set[Int]] = _data.map { case (did, dataPoint) =>
      did -> input.iterator.filter {
        case (iid, inputPoint) => dataPoint.zip(inputPoint).forall(t => t._1 >= t._2)
      }.map(_._1).toSet
    }(collection.breakOut)

    //returns both indices with locked elems filtered out ,and limits
    def processLocked(inpIds: List[Int], dti: Map[Int, Set[Int]], itd: Map[Int, Set[Int]], limits: Map[Int, List[Int]] = Map.empty): Option[(Map[Int, Set[Int]], Map[Int, Set[Int]], Map[Int, List[Int]])] = {

      if (inpIds.isEmpty) Some(dti, itd, limits)
      //itd (inut point - to covering data points) index may not contain an element only is it was already processed during previous pass
      else if(itd.contains(inpIds.head)){

        val coveringDps: Set[Int] = itd(inpIds.head)
        val inpsCoveredByDps :Set[Int]= itd.filter(_._2 == coveringDps).map(_._1)(collection.breakOut)

        if (coveringDps.isEmpty) None
        else if (inpsCoveredByDps.size==coveringDps.size){
          val newDti:Map[Int, Set[Int]] = {
            coveringDps.foldLeft(dti) { case (_dti, dp) => dti - dp }.map { case (dp, _inps) => dp -> _inps.diff(inpsCoveredByDps) }
          }
          val newItd:Map[Int, Set[Int]] = {
            inpsCoveredByDps.foldLeft(itd) { case (_itd, ip) => _itd - ip }.map { case (ip, _dps) => ip -> _dps.diff(coveringDps) }
          }

          val newLimits = limits ++ inpsCoveredByDps.map(_-> multiMax(coveringDps.map(_data)))
          //startring over with initial _inpIds
          processLocked(_inpIds, newDti, newItd, newLimits)
        } else processLocked(inpIds.tail, dti, itd, limits)

      }else processLocked(inpIds.tail, dti, itd, limits)
    }


    processLocked(input.map(_._1).toList, dataToInp, inpToData).flatMap { case (dti, itd, limits) =>

      //detects whether an unlocked sets are fully covered
      def isCovered(inps: List[Int], dti: Map[Int, Set[Int]], itd: Map[Int, Set[Int]]): Boolean = {
        inps.isEmpty || (
          dti.nonEmpty && itd.forall(_._2.nonEmpty) && {
            val minCoveredInpId = inps.minBy(i => itd(i).size)
            val minCoveringDp = itd(minCoveredInpId).minBy(d => dti(d).size)
            def newDti = (dti - minCoveringDp).map { case (d, is) => d -> (is - minCoveredInpId) }
            def newItd = (itd - minCoveredInpId).map { case (i, ds) => i -> (ds - minCoveringDp) }
            isCovered(inps.filterNot(_==minCoveredInpId), newDti, newItd)
          }
        )
      }

      if (isCovered(itd.map(_._1)(collection.breakOut), dti, itd)) Some {
        //if (non-locked) input is covered then limits are max values of covering dps
        limits ++ itd.map { case (i, d) => i -> multiMax(d.map(data)) }
      }
      else None


    }

  }

}