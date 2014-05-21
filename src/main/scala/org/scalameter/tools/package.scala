package org.scalameter

package object tools extends MeasureBuilder(Context.topLevel, Executor.Warmer.NoWarmer(), null, null) {
  type Measurer = org.scalameter.Executor.Measurer
  type MemoryFootprint = org.scalameter.Executor.Measurer.MemoryFootprint
  type DefaultWarmer = org.scalameter.Executor.Warmer.Default
  type NoWarmer = org.scalameter.Executor.Warmer.NoWarmer
  val exec = org.scalameter.Key.exec
}