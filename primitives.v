module half_adder(
    input  wire a,
    input  wire b,
    output wire sum,
    output wire carry
);
    assign sum   = a ^ b;
    assign carry = a & b;
endmodule

module full_adder(
    input  wire a,
    input  wire b,
    input  wire cin,
    output wire sum,
    output wire carry
);
    assign {carry, sum} = a + b + cin;
endmodule

