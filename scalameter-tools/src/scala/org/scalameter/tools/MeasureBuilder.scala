package org.scalameter.tools

import org.scalameter._

case class MeasureBuilder(
    context: Context,
    warmer: Executor.Warmer,
    setupBlock: () => Any,
    teardownBlock: () => Any) {
  
  def config(kvs: KeyValue*): MeasureBuilder = 
    MeasureBuilder(Context(kvs: _*), warmer, setupBlock, teardownBlock)
  
  def config(ctx: Context): MeasureBuilder = MeasureBuilder(ctx, warmer, setupBlock, teardownBlock)
  
  def withWarmer(w: Executor.Warmer): MeasureBuilder = MeasureBuilder(context, w, setupBlock, teardownBlock)
  
  def setUp(su: () => Any): MeasureBuilder = MeasureBuilder(context, warmer, su, teardownBlock)
  
  def tearDown(td: () => Any): MeasureBuilder = MeasureBuilder(context, warmer, setupBlock, td)
  
  def measure: MeasureInstance = {
    val gen = Gen.unit("tools")
    var setup:Option[Unit => Any] = None
    var teardown:Option[Unit => Any] = None
    if (this.setupBlock.isInstanceOf[() => Any])
      setup = Some(Unit => this.setupBlock.apply)
    if (this.teardownBlock .isInstanceOf[() => Any])
      teardown = Some(Unit => this.teardownBlock.apply)
    
    val configuration = Setup[Unit](context, gen, None, None, setup, teardown, None, null, null)
    new MeasureInstance(configuration, warmer)
  }
}