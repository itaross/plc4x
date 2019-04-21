#
# Autogenerated by Thrift Compiler (0.7.0)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
from pip._vendor.urllib3.connectionpool import xrange
from thrift.Thrift import *

from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol, TProtocol
try:
  from thrift.protocol import fastbinary
except:
  fastbinary = None


class RESPONSE_CODE:
  OK = 1
  NOT_FOUND = 2
  ACCESS_DENIED = 3
  INVALID_ADDRESS = 4
  INVALID_DATATYPE = 5
  INTERNAL_ERROR = 6
  RESPONSE_PENDING = 7

  _VALUES_TO_NAMES = {
    1: "OK",
    2: "NOT_FOUND",
    3: "ACCESS_DENIED",
    4: "INVALID_ADDRESS",
    5: "INVALID_DATATYPE",
    6: "INTERNAL_ERROR",
    7: "RESPONSE_PENDING",
  }

  _NAMES_TO_VALUES = {
    "OK": 1,
    "NOT_FOUND": 2,
    "ACCESS_DENIED": 3,
    "INVALID_ADDRESS": 4,
    "INVALID_DATATYPE": 5,
    "INTERNAL_ERROR": 6,
    "RESPONSE_PENDING": 7,
  }


class ConnectionHandle:
  """
  Attributes:
   - connectionId
  """

  thrift_spec = (
    None, # 0
    (1, TType.I64, 'connectionId', None, None, ), # 1
  )

  def __init__(self, connectionId=None,):
    self.connectionId = connectionId

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.I64:
          self.connectionId = iprot.readI64();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('ConnectionHandle')
    if self.connectionId is not None:
      oprot.writeFieldBegin('connectionId', TType.I64, 1)
      oprot.writeI64(self.connectionId)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class PlcException(Exception):
  """
  Attributes:
   - url
   - exceptionString
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'url', None, None, ), # 1
    (2, TType.STRING, 'exceptionString', None, None, ), # 2
  )

  def __init__(self, url=None, exceptionString=None,):
    self.url = url
    self.exceptionString = exceptionString

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.url = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRING:
          self.exceptionString = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('PlcException')
    if self.url is not None:
      oprot.writeFieldBegin('url', TType.STRING, 1)
      oprot.writeString(self.url)
      oprot.writeFieldEnd()
    if self.exceptionString is not None:
      oprot.writeFieldBegin('exceptionString', TType.STRING, 2)
      oprot.writeString(self.exceptionString)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __str__(self):
    return repr(self)

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class Request:
  """
  Attributes:
   - fields
  """

  thrift_spec = (
    None, # 0
    (1, TType.MAP, 'fields', (TType.STRING,None,TType.STRING,None), None, ), # 1
  )

  def __init__(self, fields=None,):
    self.fields = fields

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.MAP:
          self.fields = {}
          (_ktype1, _vtype2, _size0 ) = iprot.readMapBegin() 
          for _i4 in xrange(_size0):
            _key5 = iprot.readString();
            _val6 = iprot.readString();
            self.fields[_key5] = _val6
          iprot.readMapEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('Request')
    if self.fields is not None:
      oprot.writeFieldBegin('fields', TType.MAP, 1)
      oprot.writeMapBegin(TType.STRING, TType.STRING, len(self.fields))
      for kiter7,viter8 in self.fields.items():
        oprot.writeString(kiter7)
        oprot.writeString(viter8)
      oprot.writeMapEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class FieldResponse:
  """
  Attributes:
   - responseCode
   - boolValue
   - longValue
   - doubleValue
   - stringValue
  """

  thrift_spec = (
    None, # 0
    (1, TType.I32, 'responseCode', None, None, ), # 1
    (2, TType.BOOL, 'boolValue', None, None, ), # 2
    (3, TType.I64, 'longValue', None, None, ), # 3
    (4, TType.DOUBLE, 'doubleValue', None, None, ), # 4
    (5, TType.STRING, 'stringValue', None, None, ), # 5
  )

  def __init__(self, responseCode=None, boolValue=None, longValue=None, doubleValue=None, stringValue=None,):
    self.responseCode = responseCode
    self.boolValue = boolValue
    self.longValue = longValue
    self.doubleValue = doubleValue
    self.stringValue = stringValue

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.I32:
          self.responseCode = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.BOOL:
          self.boolValue = iprot.readBool();
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.I64:
          self.longValue = iprot.readI64();
        else:
          iprot.skip(ftype)
      elif fid == 4:
        if ftype == TType.DOUBLE:
          self.doubleValue = iprot.readDouble();
        else:
          iprot.skip(ftype)
      elif fid == 5:
        if ftype == TType.STRING:
          self.stringValue = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('FieldResponse')
    if self.responseCode is not None:
      oprot.writeFieldBegin('responseCode', TType.I32, 1)
      oprot.writeI32(self.responseCode)
      oprot.writeFieldEnd()
    if self.boolValue is not None:
      oprot.writeFieldBegin('boolValue', TType.BOOL, 2)
      oprot.writeBool(self.boolValue)
      oprot.writeFieldEnd()
    if self.longValue is not None:
      oprot.writeFieldBegin('longValue', TType.I64, 3)
      oprot.writeI64(self.longValue)
      oprot.writeFieldEnd()
    if self.doubleValue is not None:
      oprot.writeFieldBegin('doubleValue', TType.DOUBLE, 4)
      oprot.writeDouble(self.doubleValue)
      oprot.writeFieldEnd()
    if self.stringValue is not None:
      oprot.writeFieldBegin('stringValue', TType.STRING, 5)
      oprot.writeString(self.stringValue)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.items()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class Response:
  """
  Attributes:
   - fields
  """

  thrift_spec = (
    None, # 0
    (1, TType.MAP, 'fields', (TType.STRING,None,TType.STRUCT,(FieldResponse, FieldResponse.thrift_spec)), None, ), # 1
  )

  def __init__(self, fields=None,):
    self.fields = fields

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.MAP:
          self.fields = {}
          (_ktype10, _vtype11, _size9 ) = iprot.readMapBegin() 
          for _i13 in xrange(_size9):
            _key14 = iprot.readString();
            _val15 = FieldResponse()
            _val15.read(iprot)
            self.fields[_key14] = _val15
          iprot.readMapEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('Response')
    if self.fields is not None:
      oprot.writeFieldBegin('fields', TType.MAP, 1)
      oprot.writeMapBegin(TType.STRING, TType.STRUCT, len(self.fields))
      for kiter16,viter17 in self.fields.items():
        oprot.writeString(kiter16)
        viter17.write(oprot)
      oprot.writeMapEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.items()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)
