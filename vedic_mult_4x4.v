module vedic_mult_2x2(
    input  wire [1:0] a,
    input  wire [1:0] b,
    output wire [3:0] product
);
    assign product = a * b;
endmodule

module vedic_mult_4x4(
    input  wire [3:0] a,
    input  wire [3:0] b,
    output wire [7:0] product
);
    wire [3:0] q0;
    wire [3:0] q1;
    wire [3:0] q2;
    wire [3:0] q3;
    wire [7:0] temp0;
    wire [7:0] temp1;
    wire [7:0] temp2;
    wire [7:0] temp3;

    vedic_mult_2x2 u0(.a(a[1:0]), .b(b[1:0]), .product(q0));
    vedic_mult_2x2 u1(.a(a[3:2]), .b(b[1:0]), .product(q1));
    vedic_mult_2x2 u2(.a(a[1:0]), .b(b[3:2]), .product(q2));
    vedic_mult_2x2 u3(.a(a[3:2]), .b(b[3:2]), .product(q3));

    assign temp0 = {4'b0000, q0};
    assign temp1 = {2'b00, q1, 2'b00};
    assign temp2 = {2'b00, q2, 2'b00};
    assign temp3 = {q3, 4'b0000};

    assign product = temp0 + temp1 + temp2 + temp3;
endmodule
