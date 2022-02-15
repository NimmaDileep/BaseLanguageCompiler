package edu.udel.blc.semantic_analysis.scope

import edu.udel.blc.semantic_analysis.type.*
import edu.udel.blc.util.uranium.Reactor


object BuiltinScope : Scope() {

    override val containingScope: Scope? = null

    val _Any = PrimitiveTypeSymbol(
        name = "Any",
        containingScope = this
    )

    val _Boolean = PrimitiveTypeSymbol(
        name = "Boolean",
        containingScope = this
    )

    val _Int = PrimitiveTypeSymbol(
        name = "Int",
        containingScope = this
    )

    val _String = PrimitiveTypeSymbol(
        name = "String",
        containingScope = this
    )

    val _Unit = PrimitiveTypeSymbol(
        name = "Unit",
        containingScope = this
    )


    val print = FunctionSymbol(
        name = "print",
        containingScope = this
    )
    val print_str = VariableSymbol("str", print)

    val str = FunctionSymbol(
        name = "str",
        containingScope = this
    )
    val str_any = VariableSymbol("any", str)

    val concat = FunctionSymbol(
        name = "concat",
        containingScope = this
    )
    val concat_s1 = VariableSymbol("s1", str)
    val concat_s2 = VariableSymbol("s2", str)

    val len = FunctionSymbol(
        name = "len",
        containingScope = this
    )
    val len_array = VariableSymbol("array", len)


    init {
        declare(_Any)
        declare(_Boolean)
        declare(_Int)
        declare(_String)
        declare(_Unit)

        declare(print)
        print.declare(print_str)

        declare(str)
        str.declare(str_any)

        declare(concat)
        concat.declare(concat_s1)
        concat.declare(concat_s2)

        declare(len)
        len.declare(len_array)
    }

    fun populate(reactor: Reactor) {
        reactor[_Any, "type"] = AnyType
        reactor[_Boolean, "type"] = BooleanType
        reactor[_Int, "type"] = IntType
        reactor[_String, "type"] = StringType
        reactor[_Unit, "type"] = UnitType

        reactor[print, "type"] = FunctionType(
            parameterTypes = linkedMapOf("any" to AnyType),
            returnType = UnitType
        )
        reactor[print_str, "type"] = StringType

        reactor[str, "type"] = FunctionType(
            parameterTypes = linkedMapOf("any" to AnyType),
            returnType = StringType
        )
        reactor[str_any, "type"] = AnyType

        reactor[concat, "type"] = FunctionType(
            parameterTypes = linkedMapOf("s1" to StringType, "s2" to StringType),
            returnType = StringType
        )
        reactor[concat_s1, "type"] = StringType
        reactor[concat_s2, "type"] = StringType


        reactor[len, "type"] = FunctionType(
            parameterTypes = linkedMapOf("array" to ArrayType(AnyType)),
            returnType = IntType
        )
        reactor[len_array, "type"] = ArrayType(AnyType)
    }


}

