package hooks.test

import org.scalatest.{Spec,GivenWhenThen,FeatureSpec}
import org.scalatest.matchers.{ShouldMatchers,MustMatchers}
import hooks._

//  features
object FeatureA extends Feature {
  val name = "A"
  override def depend = List(FeatureP, FeatureNu, FeatureOmicron)
  def init() { }
}

object FeatureB extends Feature {
  val name = "B"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureC extends Feature {
  val name = "C"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureD extends Feature {
  val name = "D"
  override def depend = List(FeatureF, FeatureAlpha, FeatureKappa)
  def init() { }
}

object FeatureE extends Feature {
  val name = "E"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureF extends Feature {
  val name = "F"
  override def depend = List(FeatureZ, FeatureNu, FeatureLambda)
  def init() { }
}

object FeatureG extends Feature {
  val name = "G"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureH extends Feature {
  val name = "H"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureI extends Feature {
  val name = "I"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureJ extends Feature {
  val name = "J"
  override def depend = List(FeatureR, FeatureEpsilon, FeatureZeta)
  def init() { }
}

object FeatureK extends Feature {
  val name = "K"
  override def depend = List(FeatureZ, FeatureDelta, FeatureOmicron)
  def init() { }
}

object FeatureL extends Feature {
  val name = "L"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureM extends Feature {
  val name = "M"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureN extends Feature {
  val name = "N"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureO extends Feature {
  val name = "O"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureP extends Feature {
  val name = "P"
  override def depend = List(FeatureF, FeatureAlpha, FeatureOmega)
  def init() { }
}

object FeatureQ extends Feature {
  val name = "Q"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureR extends Feature {
  val name = "R"
  override def depend = List(FeatureZ, FeaturePi, FeatureKappa)
  def init() { }
}

object FeatureS extends Feature {
  val name = "S"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureT extends Feature {
  val name = "T"
  override def depend = List(FeatureV, FeatureMu, FeatureNu)
  def init() { }
}

object FeatureU extends Feature {
  val name = "U"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureV extends Feature {
  val name = "V"
  override def depend = List(FeatureP, FeatureDelta, FeatureTheta)
  def init() { }
}

object FeatureW extends Feature {
  val name = "W"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureX extends Feature {
  val name = "X"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureY extends Feature {
  val name = "Y"
  override def depend = Nil //List(Feature1, Feature2, Feature3)
  def init() { }
}

object FeatureZ extends Feature {
  val name = "Z"
  override def depend = List(FeatureJ, FeatureOmicron, FeatureOmega)
  def init() { }
}

object BadFeature extends Feature {
  def name = "Bad Feature"
  override def depend = Nil
  def init() {
    throw new UnsupportedOperationException("Bad Feature! Should never reach this!")
  }
}

//  Features
object FeatureAlpha extends Feature {
  def name = "α"
  override def depend = List(FeatureK, FeatureDelta, FeatureOmega)
  def init() { }
}

object FeatureBeta extends Feature {
  def name = "β"
  override def depend = Nil
  def init() { }
}

object FeatureGamma extends Feature {
  def name = "γ"
  override def depend = Nil
  def init() { }
}

object FeatureDelta extends Feature {
  def name = "δ"
  override def depend = List(FeatureX, FeatureLambda, FeatureRho)
  def init() { }
}

object FeatureEpsilon extends Feature {
  def name = "ε"
  override def depend = List(FeatureZ, FeatureGamma, FeatureKappa)
  def init() { }
}

object FeatureZeta extends Feature {
  def name = "ζ"
  override def depend = List(FeatureQ, FeatureTheta, FeaturePhi)
  def init() { }
}

object FeatureEta extends Feature {
  def name = "η"
  override def depend = Nil
  def init() { }
}

object FeatureTheta extends Feature {
  def name = "θ"
  override def depend = List(FeatureT, FeaturePhi, FeatureRho)
  def init() { }
}

object FeatureIota extends Feature {
  def name = "ι"
  override def depend = Nil
  def init() { }
}

object FeatureKappa extends Feature {
  def name = "κ"
  override def depend = List(FeatureJ, FeatureMu, FeaturePi)
  def init() { }
}

object FeatureLambda extends Feature {
  def name = "λ"
  override def depend = List(FeatureQ, FeatureGamma, FeatureLambda)
  def init() { }
}

object FeatureMu extends Feature {
  def name = "μ"
  override def depend = List(FeatureV, FeatureRho, FeatureAlpha)
  def init() { }
}

object FeatureNu extends Feature {
  def name = "ν"
  override def depend = List(FeatureS, FeatureAlpha, FeatureZeta)
  def init() { }
}

object FeatureXi extends Feature {
  def name = "ξ"
  override def depend = Nil
  def init() { }
}

object FeatureOmicron extends Feature {
  def name = "ο"
  override def depend = List(FeatureA, FeatureGamma, FeatureZeta)
  def init() { }
}

object FeaturePi extends Feature {
  def name = "π"
  override def depend = List(FeatureP, FeatureGamma, FeatureDelta)
  def init() { }
}

object FeatureRho extends Feature {
  def name = "ρ"
  override def depend = List(FeatureQ, FeatureSigma, FeatureKappa)
  def init() { }
}

object FeatureSigma extends Feature {
  def name = "σ"
  override def depend = Nil
  def init() { }
}

object FeatureTau extends Feature {
  def name = "τ"
  override def depend = Nil
  def init() { }
}

object FeatureUpsilon extends Feature {
  def name = "υ"
  override def depend = Nil
  def init() { }
}

object FeaturePhi extends Feature {
  def name = "φ"
  override def depend = Nil
  def init() { }
}

object FeatureChi extends Feature {
  def name = "χ"
  override def depend = Nil
  def init() { }
}

object FeaturePsi extends Feature {
  def name = "ψ"
  override def depend = Nil
  def init() { }
}

object FeatureOmega extends Feature {
  def name = "ω"
  override def depend = List(FeatureK, FeatureSigma, FeatureAlpha)
  def init() { }
}

class LargeSpec extends FeatureSpec with GivenWhenThen with MustMatchers {
  //  Test Hooks
  
  val actionHookKA = ActionHook[Unit]("か ka")
  val actionHookKI = ActionHook[String]("き ki")
  val actionHookKU = ActionHook[(Int, Int)]("く ku")
  val actionHookKE = ActionHook[Unit]("け ke")
  val actionHookKO = ActionHook[Unit]("こ ko")
  
  val filterHookSA = FilterHook[String]("さ sa")
  val filterHookSHI = FilterHook[Int]("し shi")
  val filterHookSU = FilterHook[Int, String]("す su")
  val filterHookSE = FilterHook[Int, String]("せ se")
  val filterHookSO = FilterHook[String]("そ so")
  
  val bufferHookTA = BufferHook("た ta")
  val bufferHookCHI = BufferHook("ち chi")
  val bufferHookTSU = BufferHook("つ tsu")
  val bufferHookTE = BufferHook("て te")
  val bufferHookTO = BufferHook("と to")
  
  val guardHookNA = GuardHook[Int]("な na")
  val guardHookNI = GuardHook[String]("に ni")
  val guardHookNU = GuardHook[Feature]("ぬ nu")
  val guardHookNE = GuardHook[String, String]("ね ne")
  val guardHookNO = GuardHook[Int]("の no")


  //  Configuration
  val allFeatures = List(FeatureA, FeatureB, FeatureC, FeatureD, FeatureE, FeatureF,
        FeatureG, FeatureH, FeatureI, FeatureJ, FeatureK, FeatureL, FeatureM,
        FeatureN, FeatureO, FeatureP, FeatureQ, FeatureR, FeatureS, FeatureT,
        FeatureU, FeatureV, FeatureW, FeatureX, FeatureY, FeatureZ)
  val reqFeatures = List(FeatureA, FeatureJ, FeatureR, FeatureZ)
  val desiredFeatures = List(FeatureD, FeatureK, FeatureT)
  val forbiddenFeatures = List(FeatureQ, FeatureU, FeatureX)
  
  val permittedFeatures = allFeatures.diff(forbiddenFeatures)
  /* val expectedFeatures = List(FeatureA, FeatureD, FeatureJ, FeatureK, FeatureR,
        FeatureT, FeatureZ, FeatureS) */
  val expectedFeatures = List(FeatureAlpha, FeatureDelta, FeatureEpsilon, FeatureZeta,
        FeatureTheta, FeatureKappa, FeatureLambda, FeatureMu, FeatureNu, FeatureOmicron,
        FeaturePi, FeatureOmega, FeatureRho, FeatureGamma, FeaturePhi, FeatureSigma)
    
  case class SecurityToken(forbidden: List[Feature])
  val securityToken = SecurityToken(forbiddenFeatures)
  
  def guardFunction(feature: FeatureLike, token: Option[Any]) = {
    token match {
      case Some(SecurityToken(forbidden)) =>
        !forbidden.contains(feature)
      case _ => true
    }
  }
  
  
  //  Utility
  def report(features: List[FeatureLike], label: String) = {
    features.length+" "+label+": "+features.sortBy(_.name).map(_.name).mkString(", ")
  }
  
  
  //  Version 2
  feature("A large system") {
    scenario("register all features") {
      val repo = FeatureRepository()
      repo.register(allFeatures: _*)

      info(report(repo.features, "features"))
      //for (f <- allFeatures)
      //  repo should have ('feature
      assert(repo.hasFeatures(allFeatures: _*))
    }
    
    scenario("require features") {
      val repo = FeatureRepository()      
      repo.require(reqFeatures: _*)
      
      info(report(repo.features, "features"))
      info(report(repo.requiredFeatures, "required"))
      assert(repo.hasFeatures(reqFeatures: _*))
      assert(repo.isRequired(reqFeatures: _*))
    }
    
    scenario("install a guard") {
      val repo = FeatureRepository()
      repo.register(allFeatures: _*)
      repo.require(reqFeatures: _*)
      repo.securityGuard.register(guardFunction _)
      assert(repo.hasFeatures(permittedFeatures: _*))
    }
    
    scenario("create a context") {
      val repo = FeatureRepository()
      repo.register(allFeatures: _*)
      repo.require(reqFeatures: _*)
      repo.securityGuard.register(guardFunction _)
      val context = repo.makeContext(desiredFeatures, securityToken)

      info(report(permittedFeatures, "permitted features"))
      info(report(context.features, "features"))
      val req = reqFeatures.diff(forbiddenFeatures)
      val des = desiredFeatures.diff(forbiddenFeatures)
      assert(req.forall(f => context.hasFeature(f)))
      assert(des.forall(f => context.hasFeature(f)))
      
      assert(expectedFeatures.forall(f => context.hasFeature(f)))
      
      val permitted = (allFeatures ::: expectedFeatures).diff(forbiddenFeatures)
      assert(context.features.forall(f => permitted.contains(f)))
    }
  }
 


}
