package com.monsanto.arch.cloudformation.model.resource

import com.monsanto.arch.cloudformation.model._
import spray.json._

import scala.annotation.implicitNotFound
import scala.language.implicitConversions

/**
 * Created by Ryan Richt on 2/28/15
 */

case class `AWS::EC2::EIP`(
  name:                   String,
  Domain:                 String,
  InstanceId:             Token[ResourceRef[`AWS::EC2::Instance`]],
  override val Condition: Option[ConditionRef] = None,
  override val DependsOn: Option[Seq[String]] = None
) extends Resource[`AWS::EC2::EIP`]{
  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}

object `AWS::EC2::EIP` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::EIP`] = jsonFormat5(`AWS::EC2::EIP`.apply)
}

case class `AWS::EC2::EIPAssociation`(
  name:                   String,
  AllocationId:           Option[Token[String]],
  InstanceId:             Token[ResourceRef[`AWS::EC2::Instance`]],
  override val Condition: Option[ConditionRef] = None,
  override val DependsOn: Option[Seq[String]] = None
) extends Resource[`AWS::EC2::EIPAssociation`]{
  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}

object `AWS::EC2::EIPAssociation` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::EIPAssociation`] = jsonFormat5(`AWS::EC2::EIPAssociation`.apply)
}

case class AMIId(id: String)
object AMIId extends DefaultJsonProtocol {
  implicit val format: JsonFormat[AMIId] = new JsonFormat[AMIId] {
    def write(obj: AMIId) = JsString(obj.id)
    def read(json: JsValue) = AMIId(json.convertTo[String])
  }
}

case class EC2MountPoint(Device: String, VolumeId: Token[String])
object EC2MountPoint extends DefaultJsonProtocol {
  implicit val format: JsonFormat[EC2MountPoint] = jsonFormat2(EC2MountPoint.apply)
}

case class `AWS::EC2::Instance`(
  name:                   String,
  InstanceType:           Token[String],
  KeyName:                Token[String],
  SubnetId:               Token[ResourceRef[`AWS::EC2::Subnet`]],
  ImageId:                Token[AMIId],
  Tags:                   Seq[AmazonTag],
  SecurityGroupIds:       Seq[ResourceRef[`AWS::EC2::SecurityGroup`]] = Seq.empty[ResourceRef[`AWS::EC2::SecurityGroup`]],
  Metadata:               Option[Map[String, String]] = None,
  IamInstanceProfile:     Option[Token[ResourceRef[`AWS::IAM::InstanceProfile`]]] = None,
  SourceDestCheck:        Option[String] = None,
  UserData:               Option[`Fn::Base64`] = None,
  Monitoring:             Option[Boolean] = None,
  Volumes:                Option[Seq[EC2MountPoint]] = None,
  PrivateIpAddress:       Option[Token[IPAddress]] = None,
  DisableApiTermination:  Option[String] = None,
  override val Condition: Option[ConditionRef] = None,
  override val DependsOn: Option[Seq[String]] = None
) extends Resource[`AWS::EC2::Instance`]{
  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::Instance` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::Instance`] = jsonFormat17(`AWS::EC2::Instance`.apply)
}

case class `AWS::EC2::InternetGateway`(name: String, Tags: Seq[AmazonTag],
  override val Condition: Option[ConditionRef] = None) extends Resource[`AWS::EC2::InternetGateway`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::InternetGateway` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::InternetGateway`] = jsonFormat3(`AWS::EC2::InternetGateway`.apply)
}

case class `AWS::EC2::KeyPair::KeyName`(name: String,
  override val Condition: Option[ConditionRef] = None) extends Resource[`AWS::EC2::KeyPair::KeyName`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::KeyPair::KeyName` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::KeyPair::KeyName`] = jsonFormat2(`AWS::EC2::KeyPair::KeyName`.apply)
}

@implicitNotFound("A Route can only have exactly ONE of GatewayId, InstanceId, NetworkInterfaceId or VpcPeeringConnectionId set")
class ValidRouteCombo[G, I, P] private ()
object ValidRouteCombo{
  implicit object valid1T extends ValidRouteCombo[Some[Token[ResourceRef[`AWS::EC2::InternetGateway`]]], None.type, None.type]
  implicit object valid1 extends ValidRouteCombo[Some[ResourceRef[`AWS::EC2::InternetGateway`]], None.type, None.type]
  implicit object valid2T extends ValidRouteCombo[None.type , Some[Token[ResourceRef[`AWS::EC2::Instance`]]], None.type]
  implicit object valid2 extends ValidRouteCombo[None.type , Some[ResourceRef[`AWS::EC2::Instance`]], None.type]
  implicit object valid3T extends ValidRouteCombo[None.type , None.type, Some[Token[ResourceRef[`AWS::EC2::VPCPeeringConnection`]]]]
  implicit object valid3 extends ValidRouteCombo[None.type , None.type, Some[ResourceRef[`AWS::EC2::VPCPeeringConnection`]]]
}

class `AWS::EC2::Route` private (
  val name:                   String,
  val RouteTableId:           Token[ResourceRef[`AWS::EC2::RouteTable`]],
  val DestinationCidrBlock:   Token[CidrBlock],
  val GatewayId:              Option[Token[ResourceRef[`AWS::EC2::InternetGateway`]]] = None,
  val InstanceId:             Option[Token[ResourceRef[`AWS::EC2::Instance`]]] = None,
  val VpcPeeringConnectionId: Option[Token[ResourceRef[`AWS::EC2::VPCPeeringConnection`]]] = None,
  override val Condition:     Option[ConditionRef] = None,
  override val DependsOn:     Option[Seq[String]] = None
) extends Resource[`AWS::EC2::Route`] {
  private val asSeq = Seq(name, RouteTableId, DestinationCidrBlock, GatewayId, InstanceId, VpcPeeringConnectionId,
    Condition, DependsOn)

  def when(newCondition: Option[ConditionRef] = Condition) =
    new `AWS::EC2::Route`(name, RouteTableId, DestinationCidrBlock, GatewayId, InstanceId, VpcPeeringConnectionId,
      newCondition, DependsOn)
}
object `AWS::EC2::Route` extends DefaultJsonProtocol {

  private def writeField[T: JsonFormat](t: T) = {
    val writer = implicitly[JsonFormat[T]]
    writer match {
      case _: OptionFormat[_] if t == None => None
      case _ => Some(writer.write(t))
    }
  }

  // Because we dont want the default case class apply method without our checks
  implicit val format: JsonFormat[`AWS::EC2::Route`] = new JsonFormat[`AWS::EC2::Route`]{
    def write(p: `AWS::EC2::Route`) = {
      JsObject(
        Map(
          "name"                   -> writeField(p.name),
          "RouteTableId"           -> writeField(p.RouteTableId),
          "DestinationCidrBlock"   -> writeField(p.DestinationCidrBlock),
          "GatewayId"              -> writeField(p.GatewayId),
          "InstanceId"             -> writeField(p.InstanceId),
          "VpcPeeringConnectionId" -> writeField(p.VpcPeeringConnectionId),
          "Condition"              -> writeField(p.Condition),
          "DependsOn"              -> writeField(p.DependsOn)
        ).filter(_._2.isDefined).mapValues(_.get)
      )
    }

    // TODO
    def read(json: JsValue) = ???
  }

  def apply[
    G <: Option[Token[ResourceRef[`AWS::EC2::InternetGateway`]]],
    I <: Option[Token[ResourceRef[`AWS::EC2::Instance`]]],
    P <: Option[Token[ResourceRef[`AWS::EC2::VPCPeeringConnection`]]]
  ](
    name:                         String,
    RouteTableId:                 Token[ResourceRef[`AWS::EC2::RouteTable`]],
    DestinationCidrBlock:         Token[CidrBlock],
    GatewayId:                    G = None,
    InstanceId:                   I = None,
    VpcPeeringConnectionId:       P = None,
    Condition: Option[ConditionRef] = None,
    DependsOn: Option[Seq[String]] = None
   )(implicit ev1: ValidRouteCombo[G, I, P]) =
    new `AWS::EC2::Route`(name, RouteTableId, DestinationCidrBlock, GatewayId, InstanceId, VpcPeeringConnectionId,
      Condition, DependsOn)
}

case class `AWS::EC2::RouteTable`(name: String, VpcId: Token[ResourceRef[`AWS::EC2::VPC`]], Tags: Seq[AmazonTag],
  override val Condition: Option[ConditionRef] = None) extends Resource[`AWS::EC2::RouteTable`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::RouteTable` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::RouteTable`] = jsonFormat4(`AWS::EC2::RouteTable`.apply)
}

case class `AWS::EC2::SecurityGroup`(
  name:                 String,
  GroupDescription:     String,
  VpcId:                Token[ResourceRef[`AWS::EC2::VPC`]],
  SecurityGroupIngress: Option[Seq[IngressSpec]],
  SecurityGroupEgress:  Option[Seq[EgressSpec]] = None,
  Tags:                 Seq[AmazonTag],
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::SecurityGroup`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::SecurityGroup` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::SecurityGroup`] = jsonFormat7(`AWS::EC2::SecurityGroup`.apply)
}

sealed trait IngressSpec
object IngressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[IngressSpec] = new JsonFormat[IngressSpec] {
    def write(obj: IngressSpec) =
      obj match {
        case i: CidrIngressSpec => i.toJson
        case i: SGIngressSpec => i.toJson
      }
    //TODO
    def read(json: JsValue) = ???
  }
}
case class CidrIngressSpec(IpProtocol: String, CidrIp: Token[CidrBlock], FromPort: String, ToPort: String) extends IngressSpec
object CidrIngressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[CidrIngressSpec] = jsonFormat4(CidrIngressSpec.apply)
}
case class SGIngressSpec(IpProtocol: String, SourceSecurityGroupId: Token[ResourceRef[`AWS::EC2::SecurityGroup`]], FromPort: String, ToPort: String) extends IngressSpec
object SGIngressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[SGIngressSpec] = jsonFormat4(SGIngressSpec.apply)
}

case class IPAddressSegment(value: Int){ require( value <= 255 && value >= 0 ) }
object IPAddressSegment {
  implicit def fromInt(i: Int): IPAddressSegment = IPAddressSegment(i)
}

case class IPMask(value: Int){ require( value <= 32 && value >= 0 ) }
object IPMask {
  implicit def fromInt(i: Int): IPMask = IPMask(i)
}

case class CidrBlock(a: IPAddressSegment, b: IPAddressSegment, c: IPAddressSegment, d: IPAddressSegment, mask: IPMask) {
  def toJsString: JsString =  JsString( Seq(a, b, c, d).map(_.value.toString).mkString(".") + "/" + mask.value.toString )
}
object CidrBlock extends DefaultJsonProtocol {
  implicit val format: JsonFormat[CidrBlock] = new JsonFormat[CidrBlock] {
    def write(obj: CidrBlock) = obj.toJsString

    def read(json: JsValue) = {
      val parts = json.convertTo[String].split(Array('.','/')).map(_.toInt)

      CidrBlock(parts(0), parts(1), parts(2), parts(3), parts(4))
    }
  }
}

case class IPAddress(a: IPAddressSegment, b: IPAddressSegment, c: IPAddressSegment, d: IPAddressSegment) {
  def toJsString: JsString =  JsString( Seq(a, b, c, d).map(_.value.toString).mkString("."))
}
object IPAddress extends DefaultJsonProtocol {
  implicit val format: JsonFormat[IPAddress] = new JsonFormat[IPAddress] {
    def write(obj: IPAddress) = obj.toJsString

    def read(json: JsValue) = {
      val parts = json.convertTo[String].split(Array('.')).map(_.toInt)

      IPAddress(parts(0), parts(1), parts(2), parts(3))
    }
  }
}

sealed trait EgressSpec
object EgressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[EgressSpec] = new JsonFormat[EgressSpec] {
    def write(obj: EgressSpec) =
      obj match {
        case i: CidrEgressSpec => i.toJson
        case i: SGEgressSpec   => i.toJson
      }
    //TODO
    def read(json: JsValue) = ???
  }
}
case class CidrEgressSpec(IpProtocol: String, CidrIp: Token[CidrBlock], FromPort: String, ToPort: String) extends EgressSpec
object CidrEgressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[CidrEgressSpec] = jsonFormat4(CidrEgressSpec.apply)
}
case class SGEgressSpec(IpProtocol: String, DestinationSecurityGroupId: Token[String], FromPort: String, ToPort: String) extends EgressSpec
object SGEgressSpec extends DefaultJsonProtocol {
  implicit val format: JsonFormat[SGEgressSpec] = jsonFormat4(SGEgressSpec.apply)
}

case class `AWS::EC2::SecurityGroupEgress`(
  name:                       String,
  GroupId:                    Token[ResourceRef[`AWS::EC2::SecurityGroup`]],
  IpProtocol:                 String,
  FromPort:                   String,
  ToPort:                     String,
  CidrIp:                     Option[Token[CidrBlock]] = None, // either CidrIp or SourceSecurityGroupId required
  DestinationSecurityGroupId: Option[Token[ResourceRef[`AWS::EC2::SecurityGroup`]]] = None, // either CidrIp or SourceSecurityGroupId required
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::SecurityGroupEgress`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::SecurityGroupEgress` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::SecurityGroupEgress`] = jsonFormat8(`AWS::EC2::SecurityGroupEgress`.apply)
}

case class `AWS::EC2::SecurityGroupIngress`(
  name:                  String,
  GroupId:               Token[ResourceRef[`AWS::EC2::SecurityGroup`]],
  IpProtocol:            String,
  FromPort:              String,
  ToPort:                String,
  CidrIp:                Option[Token[CidrBlock]] = None, // either CidrIp or SourceSecurityGroupId required
  SourceSecurityGroupId: Option[Token[ResourceRef[`AWS::EC2::SecurityGroup`]]] = None, // either CidrIp or SourceSecurityGroupId required
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::SecurityGroupIngress`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::SecurityGroupIngress` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::SecurityGroupIngress`] = jsonFormat8(`AWS::EC2::SecurityGroupIngress`.apply)
}

case class `AWS::EC2::Subnet`(
  name:             String,
  VpcId:            Token[ResourceRef[`AWS::EC2::VPC`]],
  AvailabilityZone: Token[String],
  CidrBlock:        Token[CidrBlock],
  Tags:             Seq[AmazonTag],
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::Subnet`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::Subnet` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::Subnet`] = jsonFormat6(`AWS::EC2::Subnet`.apply)
}

case class `AWS::EC2::SubnetRouteTableAssociation`(
  name:         String,
  SubnetId:     Token[ResourceRef[`AWS::EC2::Subnet`]],
  RouteTableId: Token[ResourceRef[`AWS::EC2::RouteTable`]],
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::SubnetRouteTableAssociation`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::SubnetRouteTableAssociation` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::SubnetRouteTableAssociation`] = jsonFormat4(`AWS::EC2::SubnetRouteTableAssociation`.apply)
}

case class `AWS::EC2::VPC`(name: String, CidrBlock: Token[CidrBlock], Tags: Seq[AmazonTag], EnableDnsSupport: Boolean = true, EnableDnsHostnames: Boolean = false,
  override val Condition: Option[ConditionRef] = None) extends Resource[`AWS::EC2::VPC`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::VPC` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::VPC`] = jsonFormat6(`AWS::EC2::VPC`.apply)
}

case class `AWS::EC2::VPCPeeringConnection`(
  name: String,
  PeerVpcId: Token[String],
  Tags: Seq[AmazonTag],
  VpcId: Token[String],
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::VPCPeeringConnection`] {
  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::VPCPeeringConnection` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::VPCPeeringConnection`] = jsonFormat5(`AWS::EC2::VPCPeeringConnection`.apply)
}

case class `AWS::EC2::VPCGatewayAttachment`(
  name:              String,
  VpcId:             Token[ResourceRef[`AWS::EC2::VPC`]],
  InternetGatewayId: Token[ResourceRef[`AWS::EC2::InternetGateway`]],
  override val Condition: Option[ConditionRef] = None
  ) extends Resource[`AWS::EC2::VPCGatewayAttachment`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::VPCGatewayAttachment` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::VPCGatewayAttachment`] = jsonFormat4(`AWS::EC2::VPCGatewayAttachment`.apply)
}

case class `AWS::EC2::Volume` private (
  name:                 String,
  AvailabilityZone:     Token[String],
  Encrypted:            Option[Boolean],
  Iops:                 Option[Int],
  Size:                 Option[Token[Int]],
  SnapshotId:           Option[String],
  Tags:                 Seq[AmazonTag],
  VolumeType:           String,
  override val Condition: Option[ConditionRef] = None
                               ) extends Resource[`AWS::EC2::Volume`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::Volume` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::Volume`] = jsonFormat9(`AWS::EC2::Volume`.apply)

  //require( size >= 1 && size <= 16384 )
  def gp2(name: String, az: Token[String], size: Token[Int], tags: Seq[AmazonTag], encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, Some(size), None, tags, "gp2")

  //require( size >= 4 && size <= 16384 )
  //require( iops <= size * 30 && iops >= 100 && iops <= 20000)
  def io2(name: String, az: Token[String], size: Token[Int], tags: Seq[AmazonTag], iops: Int, encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, Some(size), None, tags, "io2")

  //require( size >= 1 && size <= 1024 )
  def standard(name: String, az: Token[String], size: Token[Int], tags: Seq[AmazonTag], encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, Some(size), None, tags, "standard")

  def gp2Snapshot(name: String, az: Token[String], snapshotID: String, tags: Seq[AmazonTag], encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, None, Some(snapshotID), tags, "gp2")

  def io2Snapshot(name: String, az: Token[String], snapshotID: String, tags: Seq[AmazonTag], encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, None, Some(snapshotID), tags, "io2")

  def standardSnapshot(name: String, az: Token[String], snapshotID: String, tags: Seq[AmazonTag], encrypted: Boolean = true ) =
    `AWS::EC2::Volume`(name, az, Some(encrypted), None, None, Some(snapshotID), tags, "standard")
}

case class `AWS::EC2::VolumeAttachment`(
                                        name:         String,
                                        Device:       String,
                                        InstanceId:   ResourceRef[`AWS::EC2::Instance`],
                                        VolumeId:     ResourceRef[`AWS::EC2::Volume`],
                                        override val Condition: Option[ConditionRef] = None
) extends Resource[`AWS::EC2::VolumeAttachment`]{

  def when(newCondition: Option[ConditionRef] = Condition) = copy(Condition = newCondition)
}
object `AWS::EC2::VolumeAttachment` extends DefaultJsonProtocol {
  implicit val format: JsonFormat[`AWS::EC2::VolumeAttachment`] = jsonFormat5(`AWS::EC2::VolumeAttachment`.apply)
}
