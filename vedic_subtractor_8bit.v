module vedic_subtractor_8bit(
    input  wire [7:0] a,
    input  wire [7:0] b,
    output wire [7:0] diff,
    output wire       borrow
);
    wire [7:0] b_comp;
    wire [8:0] result_ext;

    assign b_comp     = ~b;
    assign result_ext = {1'b0, a} + {1'b0, b_comp} + 9'b000000001;
    assign diff       = result_ext[7:0];
    assign borrow     = ~result_ext[8];
endmodule

