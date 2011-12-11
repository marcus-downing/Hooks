package hooks

import scala.collection.mutable.{HashMap, ListBuffer}

/**
 * Feature Repository and Hook Context
 *
 * A repository is a store of all the potential features registered.
 * You can use either the FeatureRepository object, or create an instance.
 *
 * A context is a collection of activated features, and the hooks they've initialised.
 * The context you create should be passed around as an implicit value.
 */
 
trait FeatureRepository {
  def plugins: List[Plugin]
  def features: List[Feature]
  def requiredFeatures: List[Feature]
  def optionalFeatures: List[Feature] = features.diff(requiredFeatures)
  
  def registerPlugins(plugins: Plugin*)
  def register(features: Feature*)
  def require(features: Feature*)
  def purge(preserve: Feature*)
  
  def isEmpty = features.isEmpty
  def hasFeature(feature: Feature) = features.contains(feature)
  def hasFeatures(features: Feature*) = features.forall(f => hasFeature(f))
  def isRequired(features: Feature*) = features.forall(f => requiredFeatures.contains(f))
  
  def makeContext(desiredFeatures: List[Feature]): HookContext
  def makeContext(desiredFeatures: List[Feature], token: Any): HookContext
  
  def usingFeatures[R](desiredFeatures: List[Feature])(f: => R): R = makeContext(desiredFeatures).using(f)
  def usingFeatures[R](desiredFeatures: List[Feature], token: Any)(f: => R): R = makeContext(desiredFeatures, token).using(f)
  
  def copy = {
    val copy = new FeatureRepositoryImpl
    copy.register(this.features: _*)
    copy.require(this.requiredFeatures: _*)
    copy
  }
}

class FeatureRepositoryImpl extends FeatureRepository {
  val _registeredPlugins = ListBuffer[Plugin]()
  val _registeredFeatures = ListBuffer[Feature]()
  val _requiredFeatures = ListBuffer[Feature]()
  def plugins = _registeredPlugins.toList
  def features = _registeredFeatures.toList
  def requiredFeatures = _requiredFeatures.toList

  def registerPlugins(plugins: Plugin*) {
    _registeredPlugins.appendAll(plugins.diff(_registeredPlugins))
    for (plugin <- plugins) {
      register(plugin.optionalFeatures: _*)
      require(plugin.requiredFeatures: _*)
    }
  }

  def register(features: Feature*) {
    _registeredFeatures.appendAll(features.diff(_registeredFeatures))
  }
  def require(features: Feature*) {
    register(features: _*)
    _requiredFeatures.appendAll(features.diff(_requiredFeatures))
  }
  def purge(preserve: Feature*) {
    val keep = (preserve.toList ::: _requiredFeatures.toList).distinct
    
  }
  
  //def isEmpty: Boolean = registeredFeatures.isEmpty
  //def hasFeature(feature: Feature) = registeredFeatures.contains(feature)
  //def isRequired(feature: Feature) = requiredFeatures.contains(feature)
  
  val securityGuard = GuardHook.standalone[FeatureLike, Option[Any]]("Repository guard")
  
  //  find all feature dependencies
  def traceFeatures(head: List[FeatureLike], token: Option[Any]) = {
    def trace(past: List[FeatureLike], present: List[FeatureLike]): List[FeatureLike] = {
      if (present.isEmpty) past
      else {
        val future = for {
          f <- present
          p <- f.depend
        } yield p
        val future2 = future.distinct.filter { p =>
          !past.contains(p) && !present.contains(p) && securityGuard(p, token)
        }
        trace(present ::: past, future2)
      }
    }
    trace(Nil, head)
  }
  
  //  topological sort by the Kuhn algorithm
  def sortFeatures(in: List[FeatureLike]) = {
    def iterate(free: List[FeatureLike], edges: List[(FeatureLike, FeatureLike)], sofar: List[FeatureLike]): List[FeatureLike] = {
      //if (sofar.length > in.length) { println("So far: "+sofar.l); throw new Exception("So far: "+sofar.l+"; Features: "+features.l) }
      if (sofar.length > in.length) throw new Exception()
      free match {
        case Nil =>
          if (!edges.isEmpty) throw new FeatureDependencyException(edges)
          sofar
        case head :: tail =>
          val followedEdges = for {
            e <- edges; if e._1 == head
            m = e._2; if !edges.exists(e2 => e != e2 && e2._2 == m)
          } yield e
          val newlyfree = followedEdges.map(_._2)
          val nowfree = (newlyfree ::: free.tail).distinct
          iterate(nowfree, edges diff followedEdges, head :: sofar)
      }
    }
  
    val edges: List[(FeatureLike, FeatureLike)] = {
      val edges1: List[(FeatureLike, FeatureLike)] = for { p <- in; b <- p.before.intersect(in) } yield (b, p)
      val edges2: List[(FeatureLike, FeatureLike)] = for { p <- in; a <- p.after.intersect(in) } yield (p, a)
      (edges1 ::: edges2).distinct
    }
    val freeNodes = for { n <- in; if !edges.exists(edge => edge._2 == n) } yield n
    
    val s = iterate(freeNodes, edges, Nil)
    s
  }

  //  create a context
  def makeContext(desiredFeatures: List[Feature]): HookContext = makeContext(desiredFeatures, None)
  def makeContext(desiredFeatures: List[Feature], token: Any): HookContext = makeContext(desiredFeatures, Some(token))

  def makeContext(desiredFeatures: List[Feature], token: Option[Any]): HookContext = {
    val activeFeatures = (desiredFeatures.intersect(optionalFeatures) ::: requiredFeatures.toList).filter(f => securityGuard(f, token))
    val features = sortFeatures(traceFeatures(activeFeatures, token))
    
    //  initialise all the features in order
    val builder = new HookContextBuilderImpl(features)
    builder.using {
      for (feature <- features)
        feature.init()
    }
    val registry: Map[Hook[_], List[_]] = 
      builder.registry.toMap.mapValues(_.toList)

    new HookContextImpl(features, registry)
  }
}

class SynchronizedFeatureRepository(val inner: FeatureRepositoryImpl) extends FeatureRepository {
  def plugins = inner.synchronized { inner.plugins }
  def features = inner.synchronized { inner.features }
  def requiredFeatures = inner.synchronized { inner.requiredFeatures }
  
  def registerPlugins(plugins: Plugin*) { inner.synchronized { inner.registerPlugins(plugins: _*) } }
  def register(features: Feature*) { inner.synchronized { inner.register(features: _*) } }
  def require(features: Feature*) { inner.synchronized { inner.require(features: _*) } }
  def purge(preserve: Feature*) { inner.synchronized { inner.purge(preserve: _*) } }
  
  def makeContext(desiredFeatures: List[Feature]) =
    inner.synchronized { inner.makeContext(desiredFeatures) }
  def makeContext(desiredFeatures: List[Feature], token: Any) =
    inner.synchronized { inner.makeContext(desiredFeatures, token) }
    
  val securityGuard = inner.synchronized { inner.securityGuard.sync }
}

object FeatureRepository extends FeatureRepository {
  val instance = new SynchronizedFeatureRepository(new FeatureRepositoryImpl)
  def apply() = new FeatureRepositoryImpl

  def plugins = instance.plugins  
  def features = instance.features
  def requiredFeatures = instance.requiredFeatures

  def registerPlugins(plugins: Plugin*) { instance.registerPlugins(plugins: _*) }  
  def register(features: Feature*) { instance.register(features: _*) }
  def require(features: Feature*) { instance.require(features: _*) }
  def purge(preserve: Feature*) { instance.purge(preserve: _*) }
  
  def securityGuard = instance.inner.securityGuard
  def makeContext(desiredFeatures: List[Feature]) = instance.makeContext(desiredFeatures)
  def makeContext(desiredFeatures: List[Feature], token: Any) = instance.makeContext(desiredFeatures, token)
}
