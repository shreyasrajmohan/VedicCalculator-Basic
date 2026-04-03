module vedic_mult_8x8(
    input  wire [7:0] a,
    input  wire [7:0] b,
    output wire [15:0] product
);
    wire [7:0] q0;
    wire [7:0] q1;
    wire [7:0] q2;
    wire [7:0] q3;
    wire [15:0] temp0;
    wire [15:0] temp1;
    wire [15:0] temp2;
    wire [15:0] temp3;

    vedic_mult_4x4 m0(.a(a[3:0]), .b(b[3:0]), .product(q0));
    vedic_mult_4x4 m1(.a(a[7:4]), .b(b[3:0]), .product(q1));
    vedic_mult_4x4 m2(.a(a[3:0]), .b(b[7:4]), .product(q2));
    vedic_mult_4x4 m3(.a(a[7:4]), .b(b[7:4]), .product(q3));

    assign temp0 = {8'b00000000, q0};
    assign temp1 = {4'b0000, q1, 4'b0000};
    assign temp2 = {4'b0000, q2, 4'b0000};
    assign temp3 = {q3, 8'b00000000};

    assign product = temp0 + temp1 + temp2 + temp3;
endmodule

