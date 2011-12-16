package hooks.test

import org.scalatest.{Spec,GivenWhenThen,FeatureSpec}
import org.scalatest.matchers.{ShouldMatchers,MustMatchers}
import hooks._

//  features
object FeatureA extends Feature("A", depend = List(FeatureP, FeatureNu, FeatureOmicron)) {
  def init() { }
}

object FeatureB extends Feature("B") {
  def init() { }
}

object FeatureC extends Feature("C") {
  def init() { }
}

object FeatureD extends Feature("D", depend = List(FeatureF, FeatureAlpha, FeatureKappa)) {
  def init() { }
}

object FeatureE extends Feature("E") {
  def init() { }
}

object FeatureF extends Feature("F", depend = List(FeatureZ, FeatureNu, FeatureLambda)) {
  def init() { }
}

object FeatureG extends Feature("G") {
  def init() { }
}

object FeatureH extends Feature("H") {
  def init() { }
}

object FeatureI extends Feature("I") {
  def init() { }
}

object FeatureJ extends Feature("J", depend = List(FeatureR, FeatureEpsilon, FeatureZeta)) {
  def init() { }
}

object FeatureK extends Feature("K", depend = List(FeatureZ, FeatureDelta, FeatureOmicron)) {
  def init() { }
}

object FeatureL extends Feature("L") {
  def init() { }
}

object FeatureM extends Feature("M") {
  def init() { }
}

object FeatureN extends Feature("N") {
  def init() { }
}

object FeatureO extends Feature("O") {
  def init() { }
}

object FeatureP extends Feature("P", depend = List(FeatureF, FeatureAlpha, FeatureOmega)) {
  def init() { }
}

object FeatureQ extends Feature("Q") {
  def init() { }
}

object FeatureR extends Feature("R", depend = List(FeatureZ, FeaturePi, FeatureKappa)) {
  def init() { }
}

object FeatureS extends Feature("S") {
  def init() { }
}

object FeatureT extends Feature("T", depend = List(FeatureV, FeatureMu, FeatureNu)) {
  def init() { }
}

object FeatureU extends Feature("U") {
  def init() { }
}

object FeatureV extends Feature("V", depend = List(FeatureP, FeatureDelta, FeatureTheta)) {
  def init() { }
}

object FeatureW extends Feature("W") {
  def init() { }
}

object FeatureX extends Feature("X") {
  def init() { }
}

object FeatureY extends Feature("Y") {
  def init() { }
}

object FeatureZ extends Feature("Z", depend = List(FeatureJ, FeatureOmicron, FeatureOmega)) {
  def init() { }
}

object BadFeature extends Feature("Bad Feature") {
  def init() {
    throw new UnsupportedOperationException("Bad Feature! Should never reach this!")
  }
}

//  Features
object FeatureAlpha extends Feature("α", depend = List(FeatureK, FeatureDelta, FeatureOmega)) {
  def init() { }
}

object FeatureBeta extends Feature("β") {
  def init() { }
}

object FeatureGamma extends Feature("γ") {
  def init() { }
}

object FeatureDelta extends Feature("δ", depend = List(FeatureX, FeatureLambda, FeatureRho)) {
  def init() { }
}

object FeatureEpsilon extends Feature("ε", depend = List(FeatureZ, FeatureGamma, FeatureKappa)) {
  def init() { }
}

object FeatureZeta extends Feature("ζ", depend = List(FeatureQ, FeatureTheta, FeaturePhi)) {
  def init() { }
}

object FeatureEta extends Feature("η") {
  def init() { }
}

object FeatureTheta extends Feature("θ", depend = List(FeatureT, FeaturePhi, FeatureRho)) {
  def init() { }
}

object FeatureIota extends Feature("ι") {
  def init() { }
}

object FeatureKappa extends Feature("κ", depend = List(FeatureJ, FeatureMu, FeaturePi)) {
  def init() { }
}

object FeatureLambda extends Feature("λ", depend = List(FeatureQ, FeatureGamma)) {
  def init() { }
}

object FeatureMu extends Feature("μ", depend = List(FeatureV, FeatureRho, FeatureAlpha)) {
  def init() { }
}

object FeatureNu extends Feature("ν", depend = List(FeatureS, FeatureAlpha, FeatureZeta)) {
  def init() { }
}

object FeatureXi extends Feature("ξ") {
  def init() { }
}

object FeatureOmicron extends Feature("ο", depend = List(FeatureA, FeatureGamma, FeatureZeta)) {
  def init() { }
}

object FeaturePi extends Feature("π", depend = List(FeatureP, FeatureGamma, FeatureDelta)) {
  def init() { }
}

object FeatureRho extends Feature("ρ", depend = List(FeatureQ, FeatureSigma, FeatureKappa)) {
  def init() { }
}

object FeatureSigma extends Feature("σ") {
  def init() { }
}

object FeatureTau extends Feature("τ") {
  def init() { }
}

object FeatureUpsilon extends Feature("υ") {
  def init() { }
}

object FeaturePhi extends Feature("φ") {
  def init() { }
}

object FeatureChi extends Feature("χ") {
  def init() { }
}

object FeaturePsi extends Feature("ψ") {
  def init() { }
}

object FeatureOmega extends Feature("ω", depend = List(FeatureK, FeatureSigma, FeatureAlpha)) {
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
