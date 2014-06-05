package org.scalameter.tools

import org.scalameter._

case class MeasureBuilder(
    context: Context,
    warmer: Executor.Warmer,
    setupBlock: () => Any,
    teardownBlock: () => Any,
    setupBeforeAllBlock: () => Unit,
    teardownAfterAllBlock: () => Unit) {
  
  def config(kvs: KeyValue*): MeasureBuilder = MeasureBuilder(Context(kvs: _*), warmer, setupBlock, teardownBlock, setupBeforeAllBlock, teardownAfterAllBlock)
  
  def config(ctx: Context): MeasureBuilder = MeasureBuilder(ctx, warmer, setupBlock, teardownBlock, setupBeforeAllBlock, teardownAfterAllBlock)
  
  def withWarmer(w: Executor.Warmer): MeasureBuilder = MeasureBuilder(context, w, setupBlock, teardownBlock, setupBeforeAllBlock, teardownAfterAllBlock)
  
  def setUp(su: () => Any): MeasureBuilder = MeasureBuilder(context, warmer, su, teardownBlock, setupBeforeAllBlock, teardownAfterAllBlock)
  
  def tearDown(td: () => Any): MeasureBuilder = MeasureBuilder(context, warmer, setupBlock, td, setupBeforeAllBlock, teardownAfterAllBlock)
  
  def setUpBeforeAll(suba: () => Unit): MeasureBuilder = MeasureBuilder(context, warmer, setupBlock, teardownBlock, suba, teardownAfterAllBlock)

  def tearDownAfterAll(tdaa: () => Unit): MeasureBuilder = MeasureBuilder(context, warmer, setupBlock, teardownBlock, setupBeforeAllBlock, tdaa)
  
  def measure: MeasureInstance = {
    val gen = Gen.unit("tools")
    var setup:Option[Unit => Any] = None
    var teardown:Option[Unit => Any] = None
    var setupBeforeAll:Option[() => Unit] = None
    var teardownAfterAll:Option[() => Unit] = None
    if (this.setupBlock.isInstanceOf[() => Any])
      setup = Some(Unit => this.setupBlock.apply)
    if (this.teardownBlock.isInstanceOf[() => Any])
      teardown = Some(Unit => this.teardownBlock.apply)
    if (this.setupBeforeAllBlock.isInstanceOf[() => Unit])
      setupBeforeAll = Some(() => this.setupBeforeAllBlock.apply)
    if (this.teardownAfterAllBlock.isInstanceOf[() => Unit])
      teardownAfterAll = Some(() => this.teardownAfterAllBlock.apply)
    
    val configuration = Setup[Unit](context, gen, setupBeforeAll, teardownAfterAll, setup, teardown, None, null, null)
    new MeasureInstance(configuration, warmer)
  }
}