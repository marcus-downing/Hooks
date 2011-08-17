import org.scalatest.{Spec,GivenWhenThen,FeatureSpec}
import org.scalatest.matchers.{ShouldMatchers,MustMatchers}
import hooks._

class LargeSpec extends FeatureSpec with GivenWhenThen with MustMatchers {
  //  features
  object FeatureA extends Feature {
    val name = "A"
    def depend = List(FeatureP, PluginNu, PluginOmicron)
    def init(implicit c: PluginContextBuilder) { }
  }
    
  object FeatureB extends Feature {
    val name = "B"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureC extends Feature {
    val name = "C"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureD extends Feature {
    val name = "D"
    def depend = List(FeatureF, PluginAlpha, PluginKappa)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureE extends Feature {
    val name = "E"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureF extends Feature {
    val name = "F"
    def depend = List(FeatureZ, PluginNu, PluginLambda)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureG extends Feature {
    val name = "G"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureH extends Feature {
    val name = "H"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureI extends Feature {
    val name = "I"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureJ extends Feature {
    val name = "J"
    def depend = List(FeatureR, PluginEpsilon, PluginZeta)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureK extends Feature {
    val name = "K"
    def depend = List(FeatureZ, PluginDelta, PluginOmicron)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureL extends Feature {
    val name = "L"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureM extends Feature {
    val name = "M"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureN extends Feature {
    val name = "N"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureO extends Feature {
    val name = "O"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureP extends Feature {
    val name = "P"
    def depend = List(FeatureF, PluginAlpha, PluginOmega)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureQ extends Feature {
    val name = "Q"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureR extends Feature {
    val name = "R"
    def depend = List(FeatureZ, PluginPi, PluginKappa)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureS extends Feature {
    val name = "S"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }
    
  object FeatureT extends Feature {
    val name = "T"
    def depend = List(FeatureV, PluginMu, PluginNu)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureU extends Feature {
    val name = "U"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureV extends Feature {
    val name = "V"
    def depend = List(FeatureP, PluginDelta, PluginTheta)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureW extends Feature {
    val name = "W"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureX extends Feature {
    val name = "X"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureY extends Feature {
    val name = "Y"
    def depend = Nil //List(Plugin1, Plugin2, Plugin3)
    def init(implicit c: PluginContextBuilder) { }
  }

  object FeatureZ extends Feature {
    val name = "Z"
    def depend = List(FeatureJ, PluginOmicron, PluginOmega)
    def init(implicit c: PluginContextBuilder) { }
  }

  object BadFeature extends Feature {
    def name = "Bad Feature"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) {
      throw new UnsupportedOperationException("Bad Feature! Should never reach this!")
    }
  }


  //  plugins
  
  object PluginAlpha extends Plugin {
    def name = "α"
    def depend = List(FeatureK, PluginDelta, PluginOmega)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginBeta extends Plugin {
    def name = "β"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginGamma extends Plugin {
    def name = "γ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginDelta extends Plugin {
    def name = "δ"
    def depend = List(FeatureX, PluginLambda, PluginRho)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginEpsilon extends Plugin {
    def name = "ε"
    def depend = List(FeatureZ, PluginGamma, PluginKappa)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginZeta extends Plugin {
    def name = "ζ"
    def depend = List(FeatureQ, PluginTheta, PluginPhi)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginEta extends Plugin {
    def name = "η"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginTheta extends Plugin {
    def name = "θ"
    def depend = List(FeatureT, PluginPhi, PluginRho)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginIota extends Plugin {
    def name = "ι"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginKappa extends Plugin {
    def name = "κ"
    def depend = List(FeatureJ, PluginMu, PluginPi)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginLambda extends Plugin {
    def name = "λ"
    def depend = List(FeatureQ, PluginGamma, PluginLambda)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginMu extends Plugin {
    def name = "μ"
    def depend = List(FeatureV, PluginRho, PluginAlpha)
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginNu extends Plugin {
    def name = "ν"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginXi extends Plugin {
    def name = "ξ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginOmicron extends Plugin {
    def name = "ο"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginPi extends Plugin {
    def name = "π"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginRho extends Plugin {
    def name = "ρ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginSigma extends Plugin {
    def name = "σ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginTau extends Plugin {
    def name = "τ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginUpsilon extends Plugin {
    def name = "υ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginPhi extends Plugin {
    def name = "φ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginChi extends Plugin {
    def name = "χ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginPsi extends Plugin {
    def name = "ψ"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }

  object PluginOmega extends Plugin {
    def name = "ω"
    def depend = Nil
    def init(implicit c: PluginContextBuilder) { }
  }


  //  Configuration
  val allFeatures = List(FeatureA, FeatureB, FeatureC, FeatureD, FeatureE, FeatureF,
      FeatureG, FeatureH, FeatureI, FeatureJ, FeatureK, FeatureL, FeatureM,
      FeatureN, FeatureO, FeatureP, FeatureQ, FeatureR, FeatureS, FeatureT,
      FeatureU, FeatureV, FeatureW, FeatureX, FeatureY, FeatureZ)
  val reqFeatures = List(FeatureA, FeatureJ, FeatureR, FeatureZ)
  val desiredFeatures = List(FeatureD, FeatureK, FeatureT)
  val forbiddenFeatures = List(FeatureQ, FeatureU, FeatureX)
  
  val permittedFeatures = allFeatures.diff(forbiddenFeatures)
    
  case class SecurityToken(forbidden: List[Plugin])
  def guardFunction(plugin: Plugin)(token: Option[Any]) = {
    token match {
      case Some(SecurityToken(forbidden)) =>
        !forbidden.contains(plugin)
      case _ => true
    }
  }
  
  
  //  Utility
  def report(plugins: List[Plugin], label: String) = {
    plugins.length+" "+label+": "+plugins.sortBy(_.name).map(_.name).mkString(", ")
  }
  
  
  //  Version 2
  feature("A large system") {
    scenario("register all features") {
      val repo = PluginRepository()
      repo.register(allFeatures: _*)

      info(report(repo.features, "features"))
      assert(repo.hasFeatures(allFeatures: _*))
    }
    
    scenario("require features") {
      val repo = PluginRepository()      
      repo.require(reqFeatures: _*)
      
      info(report(repo.features, "features"))
      info(report(repo.requiredFeatures, "required"))
      assert(repo.hasFeatures(reqFeatures: _*))
      assert(repo.isRequired(reqFeatures: _*))
    }
    
    scenario("install a guard") {
      val repo = PluginRepository()
      repo.register(allFeatures: _*)
      repo.require(reqFeatures: _*)
      repo.securityGuard.registerGuard(guardFunction _)
      assert(repo.hasFeatures(permittedFeatures: _*))
    }
    
    scenario("create a context") {
      val repo = PluginRepository()
      repo.register(allFeatures: _*)
      repo.require(reqFeatures: _*)
      repo.securityGuard.registerGuard(guardFunction _)
      val context = repo.makeContext(desiredFeatures)

      info(report(context.features, "features"))
      info(report(context.plugins, "plugins"))
      val req = reqFeatures.diff(forbiddenFeatures)
      val des = desiredFeatures.diff(forbiddenFeatures)
      assert(req.forall(f => context.hasFeature(f)))
      assert(des.forall(f => context.hasFeature(f)))
    }
  }
 


}
