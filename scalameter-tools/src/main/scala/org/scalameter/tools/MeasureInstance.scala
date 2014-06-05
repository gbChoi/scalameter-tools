package org.scalameter.tools

import org.scalameter.Executor
import org.scalameter.Setup
import scala.compat.Platform

class MeasureInstance(setup:Setup[Unit], warmer:Executor.Warmer) {
  def apply(m: Measurer = new Executor.Measurer.Default)(snippet:  => Any): Double = {
    setup.setupBeforeAll()
    for (i <- warmer.warming(setup.context, setup.setupFor(Unit), setup.teardownFor(Unit)))(snippet)
    setup.teardownAfterAll()
    
    Platform.collectGarbage()
    
    setup.setupBeforeAll()    
    val result = m.measure[Unit, Unit](setup.context, setup.context.get(exec.benchRuns).get, setup.setupFor(), setup.teardownFor(), setup.regenerateFor(setup.gen.dataset.next), Unit => snippet)
    setup.teardownAfterAll()
    result.filter(_ >= 0).min
  }
  
  def bench(m: Measurer = new Executor.Measurer.Default)(afterMeasure: Double => Any)(snippet: => Any): Any = {
    val snippetResult = snippet
    Platform.collectGarbage()
    
    setup.setupBeforeAll()
    for (i <- warmer.warming(setup.context, setup.setupFor(Unit), setup.teardownFor(Unit)))(snippet)
    setup.teardownAfterAll()
    
    Platform.collectGarbage()
    
    setup.setupBeforeAll()
    val result = m.measure[Unit, Unit](setup.context, setup.context.get(exec.benchRuns).get, setup.setupFor(), setup.teardownFor(), setup.regenerateFor(setup.gen.dataset.next), Unit => snippet)    
    setup.teardownAfterAll()
    afterMeasure(result.filter(_ >= 0).min)
    snippetResult
  }
}